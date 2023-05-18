package gleaners.loader.usecase;

import gleaners.loader.domain.LoaderTarget;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface LoaderTargetRepository extends ReactiveMongoRepository<LoaderTarget, String> {
}
