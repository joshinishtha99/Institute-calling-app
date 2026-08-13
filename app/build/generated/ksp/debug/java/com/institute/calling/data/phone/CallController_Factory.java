package com.institute.calling.data.phone;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CallController_Factory implements Factory<CallController> {
  private final Provider<Context> contextProvider;

  public CallController_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallController get() {
    return newInstance(contextProvider.get());
  }

  public static CallController_Factory create(Provider<Context> contextProvider) {
    return new CallController_Factory(contextProvider);
  }

  public static CallController newInstance(Context context) {
    return new CallController(context);
  }
}
