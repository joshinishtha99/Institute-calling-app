package com.institute.calling.data.repository;

import com.institute.calling.data.remote.ApiService;
import com.institute.calling.data.remote.TokenStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NetworkCallingRepository_Factory implements Factory<NetworkCallingRepository> {
  private final Provider<ApiService> apiProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  public NetworkCallingRepository_Factory(Provider<ApiService> apiProvider,
      Provider<TokenStore> tokenStoreProvider) {
    this.apiProvider = apiProvider;
    this.tokenStoreProvider = tokenStoreProvider;
  }

  @Override
  public NetworkCallingRepository get() {
    return newInstance(apiProvider.get(), tokenStoreProvider.get());
  }

  public static NetworkCallingRepository_Factory create(Provider<ApiService> apiProvider,
      Provider<TokenStore> tokenStoreProvider) {
    return new NetworkCallingRepository_Factory(apiProvider, tokenStoreProvider);
  }

  public static NetworkCallingRepository newInstance(ApiService api, TokenStore tokenStore) {
    return new NetworkCallingRepository(api, tokenStore);
  }
}
