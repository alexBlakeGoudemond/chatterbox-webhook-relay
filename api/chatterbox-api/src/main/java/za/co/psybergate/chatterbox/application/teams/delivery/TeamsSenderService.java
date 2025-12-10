package za.co.psybergate.chatterbox.application.teams.delivery;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

public interface TeamsSenderService {

    HttpResponseDto process(GithubEventDto dto) throws InternalServerException;

    /// Executes an HTTP POST request to a Microsoft Teams webhook (or any HTTP endpoint) using a
    /// configured [CloseableHttpClient].
    ///
    /// **What this method does**
    ///
    /// - Builds an [HttpPost] request with a JSON payload and appropriate headers to
    ///   send POST requests without leveraging Spring's RestTemplate / WebClient Stack
    /// - It is important that the [HttpPost] Headers only contain `application/json`
    /// - **The following headers (often created for free) produce a 401 FORBIDDEN**:
    ///     - `Authorization`
    ///     - `Host`
    ///     - `Content-Length`
    ///     - `User-Agent`
    ///     - `Accept`
    /// - Returns the result wrapped in a typed `HttpResponseDto`.
    ///
    /// @param teamsDestination the full URL of the HTTP endpoint or Teams webhook
    /// @param jsonString the JSON payload to send as the POST body
    /// @return an `HttpResponseDto` containing the HTTP status code and optional body
    /// @throws InternalServerException if an I/O or network-related issue occurs during execution
    HttpResponseDto executeHttpPostRequest(String teamsDestination, String jsonString) throws InternalServerException;

}
