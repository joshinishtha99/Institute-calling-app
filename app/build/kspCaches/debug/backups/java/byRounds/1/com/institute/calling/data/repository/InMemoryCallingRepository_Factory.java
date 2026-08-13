package com.institute.calling.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class InMemoryCallingRepository_Factory implements Factory<InMemoryCallingRepository> {
  @Override
  public InMemoryCallingRepository get() {
    return newInstance();
  }

  public static InMemoryCallingRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InMemoryCallingRepository newInstance() {
    return new InMemoryCallingRepository();
  }

  private static final class InstanceHolder {
    private static final InMemoryCallingRepository_Factory INSTANCE = new InMemoryCallingRepository_Factory();
  }
}
