package university.enrollment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import university.enrollment.DTO.CourseDTO;
import university.enrollment.DTO.StudentDTO;
import university.enrollment.entity.Enrollment;
import university.enrollment.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enrollment")
@CrossOrigin(origins = "*")
public class Controller {
    @Autowired
    private Repository repo;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/add")
    public ResponseEntity<?> enroll(@RequestBody Enrollment dto) {
        try {
            // Validate student
            ResponseEntity<StudentDTO> studentResponse = restTemplate.getForEntity(
                    "https://studentwebapp-fnfaeacefdb9aggu.canadacentral-01.azurewebsites.net/student/" + dto.getStudentId(), StudentDTO.class);
            if (!studentResponse.getStatusCode().is2xxSuccessful() || studentResponse.getBody() == null) {
                return ResponseEntity.badRequest().body("Invalid student ID: " + dto.getStudentId());
            }

            // Validate course
            ResponseEntity<CourseDTO> courseResponse = restTemplate.getForEntity(
                    "https://coursewebapp-hqc9asafcfa7dcdd.canadacentral-01.azurewebsites.net/course/" + dto.getCourseId(), CourseDTO.class);
            if (!courseResponse.getStatusCode().is2xxSuccessful() || courseResponse.getBody() == null) {
                return ResponseEntity.badRequest().body("Invalid course ID: " + dto.getCourseId());
            }

            // Save enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(dto.getStudentId());
            enrollment.setCourseId(dto.getCourseId());
            enrollment.setEnrollmentDate(LocalDate.now());

            enrollment = repo.save(enrollment);
            dto.setId(enrollment.getId());
            dto.setEnrollmentDate(enrollment.getEnrollmentDate());

            return ResponseEntity.ok(dto);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.badRequest().body("Validation error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + ex.getMessage());
        }
    }

    @GetMapping("/all")
    public List<Enrollment> getAllEnrollments() {
        return repo.findAll().stream().map(e -> {
            Enrollment dto = new Enrollment();
            dto.setId(e.getId());
            dto.setStudentId(e.getStudentId());
            dto.setCourseId(e.getCourseId());
            dto.setEnrollmentDate(e.getEnrollmentDate());
            return dto;
        }).collect(Collectors.toList());
    }
}
