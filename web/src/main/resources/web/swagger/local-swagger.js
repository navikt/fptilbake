window.onload = function() {
  // Begin Swagger UI call region
  const ui = SwaggerUIBundle({
    urls: [
      {url: "/fptilbake/api/openapi.yaml", name: "fptilbake"},
      {url: "/k9/tilbake/api/openapi.yaml", name: "k9-tilbake"},
    ],
    "urls.primaryName": window.location.href.includes("fptilbake")  ? "fptilbake" : "k9-tilbake",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  })
  // End Swagger UI call region

  window.ui = ui
}
