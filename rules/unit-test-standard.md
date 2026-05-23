# Unit Test Standard — Backend

## Testing Strategy (Pyramid)

```
          ┌──────────────┐
          │   E2E Tests  │  ← Postman / Newman / REST-assured
          │  (few, slow) │     Full API flows in real/staging env
          ├──────────────┤
          │ Integration  │  ← TestContainers + real DB
          │    Tests     │     Multi-layer: Controller → Service → DB
          ├──────────────┤
          │  Unit Tests  │  ← Jest / JUnit 5 / xUnit
          │  (many, fast)│     Single class, all deps mocked
          └──────────────┘
```

---

## Testing Stack

| Framework | Unit | Integration | E2E |
|-----------|------|-------------|-----|
| NestJS / TS | **Jest** + `@nestjs/testing` | Jest + **Supertest** + TestContainers | Supertest / Postman |
| Spring Boot | **JUnit 5** + **Mockito** | `@SpringBootTest` + TestContainers | REST-assured |
| .NET | **xUnit** + **Moq** | `WebApplicationFactory` + TestContainers | HttpClient / Postman |

---

## File Placement

Tests live **next to** the source file (unit tests) or in dedicated `tests/` folder (integration/E2E).

```
src/modules/users/
├── services/
│   ├── user.service.ts
│   └── user.service.spec.ts        ← unit test colocated
├── controllers/
│   ├── user.controller.ts
│   └── user.controller.spec.ts     ← unit test colocated
└── repositories/
    ├── user.repository.ts
    └── user.repository.spec.ts

tests/
├── e2e/                            ← full API journey tests
│   └── user-crud.e2e-spec.ts
└── integration/                    ← multi-module with real DB
    └── user-auth-flow.spec.ts
```

---

## Unit Test Patterns

### NestJS / TypeScript (Jest)

```typescript
import { Test, TestingModule } from '@nestjs/testing';
import { UserService } from './user.service';
import { IUserRepository } from '../repositories/user.repository.interface';
import { UserNotFoundException } from '../exceptions/user-not-found.exception';

describe('UserService', () => {
  let service: UserService;
  const mockUserRepository: jest.Mocked<IUserRepository> = {
    findById: jest.fn(),
    findAll: jest.fn(),
    create: jest.fn(),
    update: jest.fn(),
    delete: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        UserService,
        { provide: 'IUserRepository', useValue: mockUserRepository },
      ],
    }).compile();
    service = module.get<UserService>(UserService);
  });

  afterEach(() => jest.clearAllMocks());

  describe('findById', () => {
    it('returns user when found', async () => {
      const user = { id: '1', email: 'test@example.com', name: 'Test' };
      mockUserRepository.findById.mockResolvedValue(user);

      const result = await service.findById('1');

      expect(result).toEqual(user);
      expect(mockUserRepository.findById).toHaveBeenCalledWith('1');
    });

    it('throws UserNotFoundException when not found', async () => {
      mockUserRepository.findById.mockResolvedValue(null);

      await expect(service.findById('999')).rejects.toThrow(UserNotFoundException);
    });
  });
});
```

### Spring Boot / Java (JUnit 5 + Mockito)

> `@Mock` + `@InjectMocks` ทำงานร่วมกับ `@Autowired` field injection ได้โดยตรง — Mockito inject mock เข้า field โดยอัตโนมัติ

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;  // Mockito inject @Mock เข้า @Autowired fields

    @Test
    void findById_shouldReturnUser_whenExists() {
        // Arrange — Entity ใช้ @Data จึงต้องสร้างด้วย setter หรือ builder
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.findById(UUID.randomUUID());

        // Assert
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRepository).findById(any(UUID.class));
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(UUID.randomUUID()))
            .isInstanceOf(UserNotFoundException.class);
    }
}
```

### .NET / C# (xUnit + Moq)

```csharp
public class UserServiceTests
{
    private readonly Mock<IUserRepository> _mockRepo;
    private readonly UserService _service;

    public UserServiceTests()
    {
        _mockRepo = new Mock<IUserRepository>();
        _service = new UserService(_mockRepo.Object);
    }

    [Fact]
    public async Task GetByIdAsync_ReturnsUser_WhenExists()
    {
        // Arrange
        var user = new User { Id = Guid.NewGuid(), Email = "test@example.com" };
        _mockRepo.Setup(r => r.GetByIdAsync(user.Id, It.IsAny<CancellationToken>()))
                 .ReturnsAsync(user);

        // Act
        var result = await _service.GetByIdAsync(user.Id, CancellationToken.None);

        // Assert
        Assert.Equal(user.Email, result.Email);
    }

    [Fact]
    public async Task GetByIdAsync_ThrowsNotFoundException_WhenMissing()
    {
        _mockRepo.Setup(r => r.GetByIdAsync(It.IsAny<Guid>(), default))
                 .ReturnsAsync((User?)null);

        await Assert.ThrowsAsync<NotFoundException>(
            () => _service.GetByIdAsync(Guid.NewGuid(), default));
    }
}
```

---

## Integration Test Pattern (Supertest + TestContainers)

```typescript
// tests/e2e/user.e2e-spec.ts
import { Test, TestingModule } from '@nestjs/testing';
import * as request from 'supertest';
import { AppModule } from '../../src/app/app.module';
import { PostgreSqlContainer } from '@testcontainers/postgresql';

describe('UserController (e2e)', () => {
  let app: INestApplication;

  beforeAll(async () => {
    const container = await new PostgreSqlContainer().start();
    process.env.DB_HOST = container.getHost();
    process.env.DB_PORT = String(container.getMappedPort(5432));

    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();
    app = moduleFixture.createNestApplication();
    await app.init();
  });

  it('GET /users/:id — returns 404 for unknown id', () => {
    return request(app.getHttpServer())
      .get('/users/nonexistent-id')
      .expect(404)
      .expect(res => {
        expect(res.body.error).toBe('UserNotFound');
      });
  });
});
```

---

## Coverage Goals

| Level | Tool | Target |
|-------|------|--------|
| Unit Tests | Jest / JUnit 5 / xUnit | ≥ 80% of services & repositories |
| Integration Tests | Supertest / SpringBootTest / WebApplicationFactory | All critical API flows |
| E2E Tests | Newman / REST-assured | Main user journeys |

---

## Naming Convention

| Type | NestJS / TS | Spring / Java | .NET / C# |
|------|-------------|---------------|-----------|
| Unit (service) | `user.service.spec.ts` | `UserServiceTest.java` | `UserServiceTests.cs` |
| Unit (controller) | `user.controller.spec.ts` | `UserControllerTest.java` | `UserControllerTests.cs` |
| Integration | `tests/integration/*.spec.ts` | `UserServiceIT.java` | `UserIntegrationTests.cs` |
| E2E | `tests/e2e/*.e2e-spec.ts` | `UserE2ETest.java` | `UserE2ETests.cs` |

---

## Test Structure Rule: AAA Pattern

Every test must follow **Arrange → Act → Assert**:

```
// Arrange — set up state and mocks
// Act     — call the unit under test
// Assert  — verify output & side effects
```

Never skip `// Arrange` or mix multiple acts in one test case.
