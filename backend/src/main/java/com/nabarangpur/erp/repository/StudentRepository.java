package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.Student;
import com.nabarangpur.erp.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE s.studentCode = :code AND s.deleted = false")
    Optional<Student> findByStudentCodeAndDeletedFalse(@Param("code") String studentCode);

    @Query("SELECT s FROM Student s WHERE s.user.id = :userId AND s.deleted = false")
    Optional<Student> findByUserIdAndDeletedFalse(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.deleted = false")
    long countByDeletedFalse();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = :status AND s.deleted = false")
    long countByStatusAndDeletedFalse(@Param("status") StudentStatus status);

    @Query("SELECT s FROM Student s WHERE s.deleted = false AND " +
           "(LOWER(s.fullName) LIKE LOWER(CONCAT('%', CONCAT(:search, '%'))) OR " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', CONCAT(:search, '%'))))")
    Page<Student> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.deleted = false AND s.department.id = :departmentId")
    Page<Student> findByDeletedFalseAndDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.deleted = false")
    Page<Student> findByDeletedFalse(Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.admissionApplication.id = :admissionId")
    Optional<Student> findByAdmissionApplicationId(@Param("admissionId") Long admissionId);
    
    @Query("SELECT s FROM Student s WHERE s.id = :id AND s.deleted = false")
    Optional<Student> findByIdAndDeletedFalse(@Param("id") Long id);
    @Query("""
    	    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    	    FROM Student s
    	    WHERE s.studentCode = :studentCode
    	""")
    	boolean existsByStudentCode(@Param("studentCode") String studentCode);
    
    
}