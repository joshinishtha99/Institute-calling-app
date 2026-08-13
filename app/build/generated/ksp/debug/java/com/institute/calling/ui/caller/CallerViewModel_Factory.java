package com.institute.calling.ui.caller;

import com.institute.calling.data.phone.CallController;
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
public final class CallerViewModel_Factory implements Factory<CallerViewModel> {
  private final Provider<CallingRepository> repositoryProvider;

  private final Provider<CallController> callControllerProvider;

  public CallerViewModel_Factory(Provider<CallingRepository> repositoryProvider,
      Provider<CallController> callControllerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.callControllerProvider = callControllerProvider;
  }

  @Override
  public CallerViewModel get() {
    return newInstance(repositoryProvider.get(), callControllerProvider.get());
  }

  public static CallerViewModel_Factory create(Provider<CallingRepository> repositoryProvider,
      Provider<CallController> callControllerProvider) {
    return new CallerViewModel_Factory(repositoryProvider, callControllerProvider);
  }

  public static CallerViewModel newInstance(CallingRepository repository,
      CallController callController) {
    return new CallerViewModel(repository, callController);
  }
}
