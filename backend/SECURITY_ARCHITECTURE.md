# 🎓 MyCampus PFE Security Architecture Documentation

## Overview
This document outlines the complete security architecture implemented for the MyCampus Academic Management System PFE project.

## Architecture Components

### 1. Security Data Seeding
**File**: `SecurityDataSeeder.java`
- **Purpose**: Initialize system with default roles, permissions, and admin user
- **Approach**: ApplicationRunner for production-ready initialization
- **Features**: Idempotent seeding, transactional, comprehensive logging

### 2. Admin Security Management
**File**: `AdminSecurityController.java`
- **Purpose**: Complete RBAC management API
- **Endpoints**: Role CRUD, Permission CRUD, Role-Permission assignments, User-Role assignments
- **Security**: Multi-level @PreAuthorize with fine-grained permissions

### 3. User Authentication Service
**File**: `UserDetailsServiceImpl.java`
- **Purpose**: Load user authorities from database
- **Features**: Permission-based authorities, role compatibility, account status handling

## Security Model

### Core Entities
- **Utilisateur**: User accounts with type and status
- **Role**: System roles (ADMIN, PROFESSEUR, ETUDIANT)
- **Permission**: Granular permissions following naming convention
- **UtilisateurRole**: Many-to-many user-role assignments
- **RolePermission**: Many-to-many role-permission assignments

### Permission Naming Convention
```
<DOMAIN>_<RESOURCE>_<ACTION>
Examples:
- SECURITE_ROLE_CREER
- ETUDIANT_MODULES_LIRE
- ADMIN_STRUCTURE_GERER
```

## Default System Configuration

### Seeded Roles
1. **ADMIN**: Full system access
2. **PROFESSEUR**: Teaching and academic management
3. **ETUDIANT**: Student data access

### Seeded Permissions
- **Security Management**: 15 permissions for RBAC administration
- **Student Access**: 7 permissions for student functionality
- **Admin Functions**: 3 high-level administrative permissions

### Default Admin Account
- **Email**: admin@mycampus.ma
- **Password**: admin123
- **Role**: ADMIN (with all permissions)

## API Endpoints

### Admin Security Controller
```
/api/admin/securite/
├── roles/ (CRUD + role-permission management)
├── permissions/ (CRUD)
├── roles/{code}/permissions/{code} (assign/remove)
└── utilisateurs/{id}/roles/{code} (assign/remove)
```

### Security Protection Levels
1. **Controller Level**: `ADMIN_SECURITE_GERER` for all admin security operations
2. **Method Level**: Specific permissions for each operation type
3. **Business Layer**: Additional validation and authorization checks

## Integration Points

### JWT Token Generation
```java
String scope = authentication.getAuthorities().stream()
    .map(GrantedAuthority::getAuthority)
    .collect(Collectors.joining(" "));
```

### Permission Evaluation
```java
@PreAuthorize("hasAuthority('SPECIFIC_PERMISSION_CODE')")
```

## Class Diagram (Mermaid)

```mermaid
classDiagram
    class Utilisateur {
        -Long id
        -String nom
        -String prenom
        -String email
        -String motDePasse
        -boolean actif
        -TypeUtilisateur typeUtilisateur
        -LocalDateTime dateCreation
    }
    
    class Role {
        -Long id
        -RolesCode nomRole
        -String description
        -boolean actif
        -LocalDateTime dateCreation
    }
    
    class Permission {
        -Long id
        -String codePermission
        -String nomPermission
        -String description
        -String module
        -LocalDateTime dateCreation
    }
    
    class UtilisateurRole {
        -Long id
        -Long idUtilisateur
        -Long idRole
        -LocalDateTime dateAttribution
        -boolean actif
    }
    
    class RolePermission {
        -Long id
        -Long idRole
        -Long idPermission
        -LocalDateTime dateAttribution
    }
    
    class RefreshToken {
        -Long id
        -String token
        -LocalDateTime expiryDate
        -Long idUtilisateur
    }
    
    class TypeUtilisateur {
        <<enumeration>>
        ADMINISTRATEUR
        PROFESSEUR
        ETUDIANT
    }
    
    class RolesCode {
        <<enumeration>>
        ADMIN
        PROFESSEUR
        ETUDIANT
    }
    
    Utilisateur ||--o{ UtilisateurRole : has
    Role ||--o{ UtilisateurRole : assigned_to
    Role ||--o{ RolePermission : has
    Permission ||--o{ RolePermission : granted_to
    Utilisateur ||--o{ RefreshToken : owns
    Utilisateur --> TypeUtilisateur : type
    Role --> RolesCode : code
```

## Security Benefits for PFE

### Academic Excellence
1. **Enterprise-Grade Architecture**: Demonstrates production-ready security design
2. **Comprehensive RBAC**: Complete role-based access control implementation
3. **Scalable Design**: System can evolve without code changes
4. **Professional Standards**: Follows industry best practices

### Defense Demonstration
1. **Immediate Functionality**: System works out-of-the-box with seeded data
2. **Live Administration**: Real-time role/permission management during presentation
3. **Security Awareness**: Shows understanding of modern security requirements
4. **Interactive Demo**: Jury can request live permission changes

### Technical Excellence
1. **Clean Architecture**: Proper separation of concerns
2. **Idempotent Operations**: Safe for repeated executions
3. **Comprehensive Logging**: Production-ready monitoring
4. **Type Safety**: Strong typing with enums and validation

## Usage Instructions

### System Startup
1. Application automatically seeds security data on first run
2. Default admin account created: admin@mycampus.ma / admin123
3. All permissions and role assignments configured automatically

### Admin Management
1. Use `/api/admin/securite/` endpoints for RBAC management
2. Create custom roles and permissions as needed
3. Assign permissions to roles and roles to users
4. Monitor system through comprehensive logging

### Security Testing
1. Test different user roles with various permission sets
2. Verify @PreAuthorize annotations work correctly
3. Validate JWT token contains proper authorities
4. Confirm access control enforcement at all levels

This security architecture provides a solid foundation for the MyCampus PFE project, demonstrating professional software engineering practices and comprehensive security implementation.
