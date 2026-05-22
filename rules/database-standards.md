# Database Standards

## Migration Rules

| Rule | Reason |
|------|--------|
| **Never edit an existing migration** | Other developers may have already applied it |
| **Every migration must have `down()`** | Enables safe rollback |
| **Migrations are atomic** | One concern per migration file |
| **Test rollback before merging** | Run `migration:revert` in CI |
| **No data logic in schema migrations** | Separate data migrations from schema migrations |

### Migration Naming Convention

```
# Format: <timestamp>_<action>_<description>
# Timestamp: YYYYMMDDHHMMSS

1716371200000-create-users-table.ts
1716371300000-add-email-index-to-users.ts
1716371400000-add-refresh-tokens-table.ts
1716371500000-alter-users-add-deleted-at.ts   ← soft delete
```

---

## Migration Examples

### TypeORM (TypeScript)

```typescript
import { MigrationInterface, QueryRunner, Table, TableIndex } from 'typeorm';

export class CreateUsersTable1716371200000 implements MigrationInterface {
  public async up(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.createTable(
      new Table({
        name: 'users',
        columns: [
          { name: 'id', type: 'uuid', isPrimary: true, default: 'uuid_generate_v4()' },
          { name: 'email', type: 'varchar', length: '255', isNullable: false, isUnique: true },
          { name: 'password_hash', type: 'varchar', length: '255', isNullable: false },
          { name: 'role', type: 'enum', enum: ['USER', 'ADMIN'], default: "'USER'" },
          { name: 'is_active', type: 'boolean', default: true },
          { name: 'created_at', type: 'timestamptz', default: 'now()' },
          { name: 'updated_at', type: 'timestamptz', default: 'now()' },
          { name: 'deleted_at', type: 'timestamptz', isNullable: true },
        ],
      }),
      true,
    );

    await queryRunner.createIndex(
      'users',
      new TableIndex({ name: 'idx_users_email', columnNames: ['email'] }),
    );
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.dropIndex('users', 'idx_users_email');
    await queryRunner.dropTable('users');
  }
}
```

### Flyway (Spring Boot / Java)

```sql
-- V1__create_users_table.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
```

### EF Core (.NET)

```csharp
public partial class CreateUsersTable : Migration
{
    protected override void Up(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.CreateTable(
            name: "Users",
            columns: table => new
            {
                Id = table.Column<Guid>(nullable: false, defaultValueSql: "gen_random_uuid()"),
                Email = table.Column<string>(maxLength: 255, nullable: false),
                PasswordHash = table.Column<string>(maxLength: 255, nullable: false),
                Role = table.Column<string>(maxLength: 20, nullable: false, defaultValue: "USER"),
                IsActive = table.Column<bool>(nullable: false, defaultValue: true),
                CreatedAt = table.Column<DateTimeOffset>(nullable: false, defaultValueSql: "NOW()"),
                UpdatedAt = table.Column<DateTimeOffset>(nullable: false, defaultValueSql: "NOW()"),
                DeletedAt = table.Column<DateTimeOffset>(nullable: true),
            },
            constraints: table => table.PrimaryKey("PK_Users", x => x.Id));

        migrationBuilder.CreateIndex("IX_Users_Email", "Users", "Email", unique: true);
    }

    protected override void Down(MigrationBuilder migrationBuilder)
        => migrationBuilder.DropTable(name: "Users");
}
```

---

## Entity / Model Standards

### Column Conventions

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID (preferred) or BIGINT auto-increment | UUID prevents enumeration attacks |
| `created_at` | TIMESTAMPTZ | Always with timezone |
| `updated_at` | TIMESTAMPTZ | Auto-update via trigger or ORM hook |
| `deleted_at` | TIMESTAMPTZ nullable | Soft delete pattern |
| Status fields | VARCHAR enum or DB enum | Document allowed values |
| Money/amounts | NUMERIC(19,4) or BIGINT (cents) | Never FLOAT for money |

### Soft Delete

Always prefer soft delete over hard delete for auditable entities:

```typescript
// TypeORM
@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true })
  email: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @DeleteDateColumn({ name: 'deleted_at', nullable: true })
  deletedAt?: Date;
}
```

---

## Query Standards

### Index Strategy

Add indexes for:
- All foreign key columns
- Columns used in `WHERE` clauses with high cardinality
- Columns used in `ORDER BY` on large tables
- Composite index when multiple columns are always queried together

```sql
-- Single column index
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- Partial index (soft-deleted records excluded from common queries)
CREATE INDEX idx_users_active ON users(email) WHERE deleted_at IS NULL;

-- Composite index (query: WHERE user_id = ? AND status = ?)
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
```

### Query Rules

| Rule | Reason |
|------|--------|
| Never `SELECT *` in production queries | Fetch only needed columns |
| Use pagination on all list queries | Prevents memory exhaustion |
| Set query timeout at repository level | Prevents long-running query attacks |
| Use parameterized queries always | SQL injection prevention |
| Avoid N+1 with eager loading or DataLoader | Performance |
| Cap `IN (...)` clause to 1000 items | DB performance |

---

## Transaction Standards

```typescript
// TypeORM — use QueryRunner for multi-step transactions
async transferFunds(fromId: string, toId: string, amount: number): Promise<void> {
  const queryRunner = this.dataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();
  try {
    await queryRunner.manager.decrement(Account, { id: fromId }, 'balance', amount);
    await queryRunner.manager.increment(Account, { id: toId }, 'balance', amount);
    await queryRunner.commitTransaction();
  } catch (err) {
    await queryRunner.rollbackTransaction();
    throw err;
  } finally {
    await queryRunner.release();
  }
}
```

---

## Database Commands

```bash
# TypeORM (NestJS)
npm run migration:generate -- src/database/migrations/MigrationName
npm run migration:run
npm run migration:revert
npm run seed:run

# Flyway (Spring Boot)
./mvnw flyway:migrate
./mvnw flyway:info
./mvnw flyway:repair

# EF Core (.NET)
dotnet ef migrations add MigrationName
dotnet ef database update
dotnet ef migrations remove          # remove last unapplied migration
dotnet ef database update 0          # revert all

# Prisma (Node TS)
npx prisma migrate dev --name migration-name
npx prisma migrate deploy            # production
npx prisma db seed
npx prisma studio                    # DB GUI
```
