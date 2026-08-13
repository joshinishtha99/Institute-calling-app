package com.institute.calling.ui.session;

import com.institute.calling.domain.repository.CallingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SessionViewModel_Factory implements Factory<SessionViewModel> {
  private final Provider<CallingRepository> repositoryProvider;

  public SessionViewModel_Factory(Provider<CallingRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SessionViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static SessionViewModel_Factory create(Provider<CallingRepository> repositoryProvider) {
    return new SessionViewModel_Factory(repositoryProvider);
  }

  public static SessionViewModel newInstance(CallingRepository repository) {
    return new SessionViewModel(repository);
  }
}
