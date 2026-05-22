# API Design Standards

## REST Conventions

### URL Structure

```
# Resource collections (plural noun, lowercase, kebab-case)
GET    /users
GET    /users/:id
POST   /users
PUT    /users/:id          # Full replace
PATCH  /users/:id          # Partial update
DELETE /users/:id

# Nested resources (max 2 levels deep)
GET    /users/:userId/orders
GET    /users/:userId/orders/:orderId

# Actions that don't map cleanly to CRUD — use verbs as sub-resource
POST   /users/:id/activate
POST   /orders/:id/cancel
POST   /auth/refresh-token

# Filtering, sorting, pagination via query params
GET    /users?status=active&role=admin&sort=createdAt&order=desc&page=1&limit=20
```

### Versioning

```
# URI versioning (recommended — explicit and cacheable)
/v1/users
/v2/users

# Never break v1 — add v2 when breaking changes are required
```

---

## Standard Response Envelope

### Success Response

```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "timestamp": "2026-05-22T10:00:00Z",
    "requestId": "uuid-v4"
  }
}
```

### Paginated Response

```json
{
  "success": true,
  "data": [ ... ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 150,
    "totalPages": 8
  },
  "meta": {
    "timestamp": "2026-05-22T10:00:00Z",
    "requestId": "uuid-v4"
  }
}
```

### Error Response

```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User with id '123' was not found.",
    "details": []
  },
  "meta": {
    "timestamp": "2026-05-22T10:00:00Z",
    "requestId": "uuid-v4"
  }
}
```

### Validation Error Response (422)

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed.",
    "details": [
      { "field": "email", "message": "must be a valid email address" },
      { "field": "age", "message": "must be a positive number" }
    ]
  }
}
```

---

## HTTP Status Codes

| Status | When to Use |
|--------|-------------|
| `200 OK` | Successful GET, PUT, PATCH |
| `201 Created` | Successful POST (resource created) — include `Location` header |
| `204 No Content` | Successful DELETE or action with no body |
| `400 Bad Request` | Malformed request syntax, invalid parameters |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but not authorized |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Duplicate resource, optimistic lock failure |
| `422 Unprocessable Entity` | Validation errors on well-formed request |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Unexpected server error (never expose stack traces) |
| `503 Service Unavailable` | Upstream dependency down, circuit breaker open |

---

## OpenAPI / Swagger Annotation

Every endpoint must have complete OpenAPI documentation:

### NestJS Example

```typescript
@ApiTags('users')
@Controller('v1/users')
export class UserController {

  @Get(':id')
  @ApiOperation({ summary: 'Get user by ID' })
  @ApiParam({ name: 'id', type: String, description: 'User UUID' })
  @ApiResponse({ status: 200, description: 'User found', type: UserResponseDto })
  @ApiResponse({ status: 404, description: 'User not found', type: ErrorResponseDto })
  @ApiResponse({ status: 401, description: 'Unauthorized', type: ErrorResponseDto })
  async findOne(@Param('id') id: string): Promise<ApiResponseDto<UserResponseDto>> {
    return this.userService.findById(id);
  }

  @Post()
  @ApiOperation({ summary: 'Create new user' })
  @ApiBody({ type: CreateUserDto })
  @ApiResponse({ status: 201, description: 'User created', type: UserResponseDto })
  @ApiResponse({ status: 409, description: 'Email already exists', type: ErrorResponseDto })
  @ApiResponse({ status: 422, description: 'Validation error', type: ValidationErrorDto })
  async create(@Body() dto: CreateUserDto): Promise<ApiResponseDto<UserResponseDto>> {
    return this.userService.create(dto);
  }
}
```

### Spring Boot Example

```java
@Tag(name = "users", description = "User management")
@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Operation(summary = "Get user by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }
}
```

---

## Request DTO Validation

### NestJS (class-validator)

```typescript
import { IsEmail, IsString, MinLength, IsEnum, IsOptional } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateUserDto {
  @ApiProperty({ example: 'john@example.com' })
  @IsEmail()
  email: string;

  @ApiProperty({ minLength: 8 })
  @IsString()
  @MinLength(8)
  password: string;

  @ApiProperty({ enum: UserRole, default: UserRole.USER })
  @IsEnum(UserRole)
  @IsOptional()
  role?: UserRole = UserRole.USER;
}
```

### Spring Boot (Jakarta Validation)

```java
public record CreateUserRequest(
    @NotBlank @Email
    String email,

    @NotBlank @Size(min = 8, max = 100)
    String password,

    @NotNull
    UserRole role
) {}
```

### .NET (FluentValidation)

```csharp
public class CreateUserRequestValidator : AbstractValidator<CreateUserRequest>
{
    public CreateUserRequestValidator()
    {
        RuleFor(x => x.Email).NotEmpty().EmailAddress();
        RuleFor(x => x.Password).NotEmpty().MinimumLength(8);
        RuleFor(x => x.Role).IsInEnum();
    }
}
```

---

## Pagination Convention

Always use cursor-based pagination for large datasets; offset-based for simple use cases:

```typescript
// Offset-based query params
export class PaginationDto {
  @IsOptional() @IsInt() @Min(1) @Transform(({ value }) => parseInt(value))
  page: number = 1;

  @IsOptional() @IsInt() @Min(1) @Max(100) @Transform(({ value }) => parseInt(value))
  limit: number = 20;
}
```

---

## API Design Rules

| Rule | Reason |
|------|--------|
| Always version APIs from v1 | Prevents breaking consumers when API evolves |
| Never return raw entities | Expose only DTOs — prevents over-fetching & leaking internals |
| Use consistent error codes (UPPER_SNAKE) | Client can handle programmatically |
| Include `requestId` in all responses | Enables log correlation & debugging |
| Never expose stack traces in 5xx | Security — log server-side only |
| Idempotency key for POST mutations | Safe retry on network failure |
