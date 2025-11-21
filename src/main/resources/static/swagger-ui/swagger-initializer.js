window.onload = function() {
  // Plugin to call server logout when Swagger UI logout is clicked
  const ServerLogoutPlugin = function() {
    return {
      statePlugins: {
        auth: {
          wrapActions: {
            logout: (oriAction, system) => (payload) => {
              // Call server logout endpoint to invalidate session
              fetch("/api/v1/logout", {
                method: "POST",
                credentials: "include"
              }).catch(() => {});
              // Then do the original Swagger UI logout
              return oriAction(payload);
            }
          }
        }
      }
    };
  };

  window.ui = SwaggerUIBundle({
    url: "/api/v1/v3/api-docs",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl,
      ServerLogoutPlugin
    ],
    layout: "StandaloneLayout",
    configUrl: "/api/v1/v3/api-docs/swagger-config"
  });
};
