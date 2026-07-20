package com.nabarangpur.erp.service;

import com.nabarangpur.erp.dto.user.AssignPrivilegesRequest;
import com.nabarangpur.erp.dto.user.CreateRoleRequest;
import com.nabarangpur.erp.dto.user.RoleResponse;
import com.nabarangpur.erp.entity.Privilege;
import com.nabarangpur.erp.entity.Role;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.ConflictException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.PrivilegeRepository;
import com.nabarangpur.erp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements Functional Spec 6.1: role & privilege management.
 * "A role without privileges cannot perform actions" and
 * "System-level roles such as Super Admin cannot be deleted accidentally."
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(RoleResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listPrivilegeCodes() {
        return privilegeRepository.findAll().stream().map(Privilege::getCode).collect(Collectors.toList());
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ConflictException("Role already exists: " + request.getName());
        }
        Set<Privilege> privileges = resolvePrivileges(request.getPrivilegeCodes());
        Role role = Role.builder()
                .name(request.getName().toUpperCase())
                .description(request.getDescription())
                .systemRole(false)
                .privileges(privileges)
                .build();
        role = roleRepository.save(role);
        auditService.record("ROLE_CREATE", "Role", role.getId(), null, RoleResponse.from(role));
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse assignPrivileges(Long roleId, AssignPrivilegesRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        RoleResponse before = RoleResponse.from(role);
        role.setPrivileges(resolvePrivileges(request.getPrivilegeCodes()));
        role = roleRepository.save(role);
        auditService.record("ROLE_PRIVILEGE_ASSIGN", "Role", role.getId(), before, RoleResponse.from(role));
        return RoleResponse.from(role);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        if (role.isSystemRole()) {
            throw new BadRequestException("System-level roles cannot be deleted");
        }
        roleRepository.delete(role);
        auditService.record("ROLE_DELETE", "Role", roleId, RoleResponse.from(role), null);
    }

    private Set<Privilege> resolvePrivileges(Set<String> codes) {
        if (codes == null) return new HashSet<>();
        Set<Privilege> privileges = new HashSet<>();
        for (String code : codes) {
            privileges.add(privilegeRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Unknown privilege code: " + code)));
        }
        return privileges;
    }
}
