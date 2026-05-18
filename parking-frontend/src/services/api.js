const BASE = "http://localhost:8080/api";

async function request(path, options) {
  const response = await fetch(`${BASE}${path}`, options);

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with status ${response.status}`);
  }

  return response.json();
}

const jsonOptions = (body) => ({
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(body),
});

export const getFloor      = (n)                    => request(`/parking/floor/${n}`);
export const occupySpot    = (spotId, vehicleId)    => request("/parking/occupy",  jsonOptions({ spotId, vehicleId }));
export const releaseSpot   = (spotId)               => request("/parking/release", jsonOptions({ spotId }));
export const findVehicle   = (vehicleId)            => request(`/parking/find/${vehicleId}`);

// mode = "CAR" or "FOOT"
export const getPath = (from, to, mode = "FOOT") =>
  request(`/navigation/path?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&mode=${mode}`);

export const compareAllocation  = (vehicleType) => request(`/allocation/compare?vehicleType=${encodeURIComponent(vehicleType)}`);
export const getOptimalSchedule = ()            => request("/reservations");
export const getUtilization     = ()            => request("/utilization");