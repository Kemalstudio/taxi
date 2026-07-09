import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { AdminDriver, OnlineDriver, PlatformStats, Ride, RideStatus } from "../types";

export function useStats() {
  return useQuery({
    queryKey: ["stats"],
    queryFn: async () => (await api.get<PlatformStats>("/admin/stats")).data,
    refetchInterval: 5000,
  });
}

export function useRides(status: RideStatus | "ALL", limit = 50) {
  return useQuery({
    queryKey: ["rides", status, limit],
    queryFn: async () => {
      const params: Record<string, string | number> = { limit };
      if (status !== "ALL") params.status = status;
      return (await api.get<Ride[]>("/admin/rides", { params })).data;
    },
    refetchInterval: 5000,
  });
}

export function useDrivers() {
  return useQuery({
    queryKey: ["drivers"],
    queryFn: async () => (await api.get<AdminDriver[]>("/admin/drivers")).data,
    refetchInterval: 10000,
  });
}

export function useOnlineDrivers() {
  return useQuery({
    queryKey: ["online-drivers"],
    queryFn: async () => (await api.get<OnlineDriver[]>("/admin/drivers/online")).data,
    refetchInterval: 4000,
  });
}
