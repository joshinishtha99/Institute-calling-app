package com.institute.calling.ui.owner;

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
public final class CallReviewViewModel_Factory implements Factory<CallReviewViewModel> {
  private final Provider<CallingRepository> repositoryProvider;

  public CallReviewViewModel_Factory(Provider<CallingRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CallReviewViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CallReviewViewModel_Factory create(Provider<CallingRepository> repositoryProvider) {
    return new CallReviewViewModel_Factory(repositoryProvider);
  }

  public static CallReviewViewModel newInstance(CallingRepository repository) {
    return new CallReviewViewModel(repository);
  }
}
