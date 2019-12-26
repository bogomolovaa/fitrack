package bogomolov.aa.fitrack.dagger;

import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.repository.RepositoryImpl;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class MainModule {
    @Binds
    public abstract Repository getRepository(RepositoryImpl repository);
}
