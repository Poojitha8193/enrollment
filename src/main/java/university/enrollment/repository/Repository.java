package university.enrollment.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import university.enrollment.entity.Enrollment;


@Component
public interface Repository extends JpaRepository<Enrollment, Long> {

}
