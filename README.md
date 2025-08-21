# Deblock Flights App
This app aggregates flight results - initially from 2 different suppliers (CrazyAir and ToughJet). The app is designed to be extensible for additional suppliers in the future.


## REST API Endpoint
| Method| Url | Action                                                |
|-------|-----|-------------------------------------------------------|
| POST   | /api/flights  | retrieves aggregated flights' results ordered by fare |


**Request (FlightRequestDTO)**

| Name | Description |
| ------ | ------ |
| origin | 3 letter IATA code(eg. KRK, DPS) |
| destination | 3 letter IATA code(eg. KRK, DPS) |
| departureDate | ISO_LOCAL_DATE format |
| returnDate | ISO_LOCAL_DATE format |
| numberOfPassengers | Maximum 4 passengers |

**Response (FlightResponseDTO)**

| Name | Description                       |
| ------ |-----------------------------------|
| airline | Name of Airline                   |
| supplier | Eg: CrazyAir or ToughJet          |
| fare | Total price rounded to 2 decimals |
| departureAirportCode | 3 letter IATA code(eg. KRK, DPS)  |
| destinationAirportCode | 3 letter IATA code(eg. KRK, DPS)  |
| departureDate | ISO_DATE_TIME format              |
| arrivalDate | ISO_DATE_TIME format              |



## Architecture
The application follows **Hexagonal Architecture** (also known as Ports & Adapters), with three main layers: **Domain**, **Application**, and **Adapter**.

* **Domain**
    * Contains the core business models: `Flight` and `FlightRequest`
    * Defines the `FlightsServiceUseCase` interface

* **Application**
    * Implements the domain interfaces
    * `FlightsService` is the main service implementing `FlightsServiceUseCase`

* **Adapter**
    * The most extensive part - bridges between the application and external systems (suppliers) or users (API clients).
    * **Inbound adapters**: Connect to external flight suppliers (`CrazyAir` and `ToughJet`). Possible future extension for additional suppliers.
    * **Outbound adapters**: Provide the REST API interface. Includes:
        * `FlightsController` – handles HTTP requests
        * `FlightsMapper` – maps between domain models and DTOs
        * DTOs: `FlightRequestDTO` and `FlightResponseDTO`
        * `ControllerAdvice` – centralized error handling for API responses