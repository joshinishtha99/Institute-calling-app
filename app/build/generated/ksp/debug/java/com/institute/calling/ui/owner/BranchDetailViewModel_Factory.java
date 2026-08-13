package com.institute.calling.ui.owner;

import androidx.lifecycle.SavedStateHandle;
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
public final class BranchDetailViewModel_Factory implements Factory<BranchDetailViewModel> {
  private final Provider<CallingRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public BranchDetailViewModel_Factory(Provider<CallingRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public BranchDetailViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static BranchDetailViewModel_Factory create(Provider<CallingRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new BranchDetailViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static BranchDetailViewModel newInstance(CallingRepository repository,
      SavedStateHandle savedStateHandle) {
    return new BranchDetailViewModel(repository, savedStateHandle);
  }
}
