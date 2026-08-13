package com.institute.calling.data.remote;

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
public final class TokenStore_Factory implements Factory<TokenStore> {
  @Override
  public TokenStore get() {
    return newInstance();
  }

  public static TokenStore_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TokenStore newInstance() {
    return new TokenStore();
  }

  private static final class InstanceHolder {
    private static final TokenStore_Factory INSTANCE = new TokenStore_Factory();
  }
}
