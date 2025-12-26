# Flyway Database Migrations

This directory contains Flyway database migration scripts for API Gateway.

## Naming Convention

Migration files must follow this naming pattern:
```
V{version}__{description}.sql
```

Examples:
- `V1__Initial_schema.sql`
- `V2__Add_user_roles.sql`
- `V3__Add_audit_fields.sql`

## Rules

1. **Version numbers** must be unique and sequential
2. **Descriptions** use underscores instead of spaces
3. **Never modify** existing migration files after they've been applied
4. **Always test** migrations in development before production

## Current Schema

The baseline version is **V0** (existing schema).
New migrations should start from **V1**.

## Useful Commands

### Check migration status
```bash
mvn flyway:info
```

### Apply migrations
```bash
mvn flyway:migrate
```

### Repair checksums (if needed)
```bash
mvn flyway:repair
```

## Documentation

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Naming Patterns](https://flywaydb.org/documentation/concepts/migrations#naming)

