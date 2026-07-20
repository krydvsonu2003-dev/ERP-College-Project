-- ============================================================
-- V7: Seed roles, privileges, role-privilege mapping & master data
-- NOTE: The Super Admin user itself is created at application startup
-- (see DataSeeder.java) so its password is hashed with the live
-- PasswordEncoder bean rather than a hard-coded hash in SQL.
-- ============================================================

-- ---------- Roles ----------
INSERT INTO roles (name, description, is_system_role) VALUES
 ('SUPER_ADMIN', 'Full access to all modules and settings', TRUE),
 ('PRINCIPAL', 'College-wide view, approval and monitoring access', TRUE),
 ('HOD', 'Department-level academic operations', FALSE),
 ('FACULTY', 'Attendance, marks entry and assigned class/student data', FALSE),
 ('STUDENT', 'Self-service access to own profile, attendance, results, fees', FALSE),
 ('ACCOUNTANT', 'Fee structures, payment collection, financial reports', FALSE),
 ('ADMISSION_OFFICE_STAFF', 'Initiate and process admission applications', FALSE);

-- ---------- Privileges ----------
INSERT INTO privileges (code, module, action, description) VALUES
 ('USER_CREATE','USER','CREATE','Create user accounts'),
 ('USER_READ','USER','READ','View user accounts'),
 ('USER_UPDATE','USER','UPDATE','Edit / activate / deactivate / reset password'),
 ('ROLE_CREATE','ROLE','CREATE','Create roles & assign privileges'),
 ('ROLE_READ','ROLE','READ','View roles & privileges'),
 ('ROLE_UPDATE','ROLE','UPDATE','Edit roles & privilege assignment'),
 ('ROLE_DELETE','ROLE','DELETE','Delete non-system roles'),

 ('STUDENT_CREATE','STUDENT','CREATE','Create student record'),
 ('STUDENT_READ','STUDENT','READ','View student record'),
 ('STUDENT_UPDATE','STUDENT','UPDATE','Edit student record'),
 ('STUDENT_DELETE','STUDENT','DELETE','Remove/withdraw student record'),
 ('STUDENT_APPROVE','STUDENT','APPROVE','Approve student status changes'),

 ('FACULTY_CREATE','FACULTY','CREATE','Create faculty profile'),
 ('FACULTY_READ','FACULTY','READ','View faculty profile'),
 ('FACULTY_UPDATE','FACULTY','UPDATE','Edit faculty profile'),
 ('FACULTY_DELETE','FACULTY','DELETE','Remove faculty profile'),

 ('ADMISSION_CREATE','ADMISSION','CREATE','Submit admission application'),
 ('ADMISSION_READ','ADMISSION','READ','View admission applications'),
 ('ADMISSION_UPDATE','ADMISSION','UPDATE','Edit / verify admission applications'),
 ('ADMISSION_APPROVE','ADMISSION','APPROVE','Approve or reject admission applications'),

 ('ATTENDANCE_CREATE','ATTENDANCE','CREATE','Mark attendance'),
 ('ATTENDANCE_READ','ATTENDANCE','READ','View attendance'),
 ('ATTENDANCE_UPDATE','ATTENDANCE','UPDATE','Edit attendance records'),
 ('ATTENDANCE_APPROVE','ATTENDANCE','APPROVE','Approve late/cutoff attendance entries'),

 ('EXAMINATION_CREATE','EXAMINATION','CREATE','Enter marks / create exam schedules'),
 ('EXAMINATION_READ','EXAMINATION','READ','View exams, marks & results'),
 ('EXAMINATION_UPDATE','EXAMINATION','UPDATE','Update / recalculate marks & results'),
 ('EXAMINATION_APPROVE','EXAMINATION','APPROVE','Approve recalculation / corrections'),
 ('EXAMINATION_PUBLISH','EXAMINATION','PUBLISH','Publish results'),

 ('FEES_CREATE','FEES','CREATE','Configure fee structure / collect payment'),
 ('FEES_READ','FEES','READ','View fee structure, dues & receipts'),
 ('FEES_UPDATE','FEES','UPDATE','Edit fee structure / waivers'),
 ('FEES_DELETE','FEES','DELETE','Reverse a posted payment'),

 ('REPORTS_READ','REPORTS','READ','View reports'),
 ('REPORTS_DOWNLOAD','REPORTS','DOWNLOAD','Export reports to PDF/Excel'),

 ('AUDIT_READ','AUDIT','READ','View audit logs'),
 ('DASHBOARD_READ','DASHBOARD','READ','View role dashboard'),
 ('MASTER_DATA_MANAGE','MASTER_DATA','UPDATE','Manage departments, courses, subjects, fee heads etc.');

-- ---------- Role -> Privilege mapping ----------

-- SUPER_ADMIN: every privilege
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r CROSS JOIN privileges p WHERE r.name = 'SUPER_ADMIN';

-- PRINCIPAL: read + approval/publish across modules
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.name = 'PRINCIPAL' AND p.code IN (
 'STUDENT_READ','STUDENT_APPROVE','FACULTY_READ','ADMISSION_READ','ADMISSION_APPROVE',
 'ATTENDANCE_READ','EXAMINATION_READ','EXAMINATION_APPROVE','EXAMINATION_PUBLISH',
 'FEES_READ','REPORTS_READ','REPORTS_DOWNLOAD','DASHBOARD_READ','AUDIT_READ');

-- HOD: department-level academic operations
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.name = 'HOD' AND p.code IN (
 'STUDENT_READ','STUDENT_UPDATE','FACULTY_READ','ADMISSION_READ',
 'ATTENDANCE_READ','ATTENDANCE_APPROVE','EXAMINATION_READ','EXAMINATION_APPROVE',
 'REPORTS_READ','DASHBOARD_READ');

-- FACULTY: class/subject scoped access
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.name = 'FACULTY' AND p.code IN (
 'ATTENDANCE_CREATE','ATTENDANCE_READ','ATTENDANCE_UPDATE',
 'EXAMINATION_CREATE','EXAMINATION_READ','EXAMINATION_UPDATE',
 'STUDENT_READ','DASHBOARD_READ');

-- STUDENT: self-service only
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.name = 'STUDENT' AND p.code IN (
 'STUDENT_READ','ATTENDANCE_READ','EXAMINATION_READ','FEES_READ',
 'ADMISSION_READ','DASHBOARD_READ');

-- ACCOUNTANT: fee-related access only
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.name = 'ACCOUNTANT' AND p.code IN (
 'FEES_CREATE','FEES_READ','FEES_UPDATE','FEES_DELETE',
 'REPORTS_READ','REPORTS_DOWNLOAD','DASHBOARD_READ','STUDENT_READ');

-- ADMISSION_OFFICE_STAFF
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.name = 'ADMISSION_OFFICE_STAFF' AND p.code IN (
 'ADMISSION_CREATE','ADMISSION_READ','ADMISSION_UPDATE','DASHBOARD_READ');

-- ---------- Master data ----------

INSERT INTO departments (name, code) VALUES
 ('Department of Pharmacy', 'PHARM'),
 ('Department of Pharmaceutical Chemistry', 'PCHEM');

INSERT INTO courses (department_id, name, code, duration_years, total_semesters)
SELECT id, 'Bachelor of Pharmacy', 'BPHARM', 4, 8 FROM departments WHERE code = 'PHARM';

INSERT INTO courses (department_id, name, code, duration_years, total_semesters)
SELECT id, 'Diploma in Pharmacy', 'DPHARM', 2, 4 FROM departments WHERE code = 'PHARM';

INSERT INTO academic_years (name, start_date, end_date, is_current) VALUES
 ('2026-27', '2026-07-01', '2027-04-30', TRUE);

INSERT INTO grade_master (grade_letter, min_percentage, max_percentage, grade_point, is_pass) VALUES
 ('O',  90, 100, 10, TRUE),
 ('A+', 80, 89.99, 9, TRUE),
 ('A',  70, 79.99, 8, TRUE),
 ('B+', 60, 69.99, 7, TRUE),
 ('B',  50, 59.99, 6, TRUE),
 ('C',  40, 49.99, 5, TRUE),
 ('F',  0,  39.99, 0, FALSE);

INSERT INTO mark_components (code, name, weight_percentage) VALUES
 ('INTERNAL', 'Internal Assessment', 20),
 ('PRACTICAL', 'Practical', 20),
 ('VIVA', 'Viva Voce', 10),
 ('EXTERNAL', 'External / End-Term', 50);

INSERT INTO fee_heads (name, code, description) VALUES
 ('Admission Fee', 'ADM_FEE', 'One-time fee at admission'),
 ('Tuition Fee', 'TUITION', 'Semester tuition fee'),
 ('Examination Fee', 'EXAM_FEE', 'Per-semester examination fee'),
 ('Hostel Fee', 'HOSTEL_FEE', 'Hostel accommodation fee (optional)'),
 ('Library Fee', 'LIB_FEE', 'Annual library fee');
