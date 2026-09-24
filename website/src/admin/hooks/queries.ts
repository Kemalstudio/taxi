import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import { AdminDriverSocket } from "../lib/adminDriverSocket";
import type {
  AdminDriver,
  AdminPromoCode,
  AdminRideDetail,
  AdminUser,
  AuditLogEntry,
  BroadcastLogEntry,
  BroadcastSegment,
  ChatMessage,
  DiscountType,
  DocumentKind,
  DriverActivityStats,
  DriverEarnings,
  OnlineDriver,
  OtpChallenge,
  Payout,
  PlatformRevenue,
  PlatformStats,
  Ride,
  RideStatus,
  Role,
  SosAlert,
  SystemStatus,
  TariffRevenuePoint,
} from "../types";

export function useStats() {
  return useQuery({
    queryKey: ["stats"],
    queryFn: async () => (await api.get<PlatformStats>("/admin/stats")).data,
    refetchInterval: 5000,
  });
}

export function useRides(status: RideStatus | "ALL", limit = 50, driverId?: string, enabled = true) {
  return useQuery({
    queryKey: ["rides", status, limit, driverId],
    queryFn: async () => {
      const params: Record<string, string | number> = { limit };
      if (status !== "ALL") params.status = status;
      if (driverId) params.driverId = driverId;
      return (await api.get<Ride[]>("/admin/rides", { params })).data;
    },
    refetchInterval: 5000,
    enabled,
  });
}

export function useDrivers(enabled = true) {
  return useQuery({
    queryKey: ["drivers"],
    queryFn: async () => (await api.get<AdminDriver[]>("/admin/drivers")).data,
    refetchInterval: 10000,
    enabled,
  });
}

export function useOnlineDrivers(enabled = true) {
  return useQuery({
    queryKey: ["online-drivers"],
    queryFn: async () => (await api.get<OnlineDriver[]>("/admin/drivers/online")).data,
    // Kept as a resync safety net — useLiveDriverUpdates() pushes faster updates in between polls.
    refetchInterval: 4000,
    enabled,
  });
}

/** Subscribes to the admin-only WebSocket driver feed and merges updates into the
 * `["online-drivers"]` query cache in real time, between polls. Mount once near the live map. */
export function useLiveDriverUpdates(enabled = true) {
  const queryClient = useQueryClient();
  useEffect(() => {
    if (!enabled) return;
    const socket = new AdminDriverSocket({
      onLocation: (msg) => {
        queryClient.setQueryData<OnlineDriver[]>(["online-drivers"], (prev = []) => [
          ...prev.filter((d) => d.driverId !== msg.driverId),
          { driverId: msg.driverId, lat: msg.lat, lng: msg.lng },
        ]);
      },
      onStatus: (msg) => {
        queryClient.setQueryData<AdminDriver[]>(["drivers"], (prev = []) =>
          prev.map((d) => d.userId === msg.driverId ? { ...d, status: msg.status as AdminDriver["status"] } : d),
        );
        if (msg.status !== "OFFLINE") return;
        queryClient.setQueryData<OnlineDriver[]>(["online-drivers"], (prev = []) =>
          prev.filter((d) => d.driverId !== msg.driverId),
        );
      },
    });
    socket.connect();
    return () => socket.disconnect();
  }, [queryClient, enabled]);
}

export function useSos(limit = 20, enabled = true) {
  return useQuery({
    queryKey: ["sos", limit],
    queryFn: async () => (await api.get<SosAlert[]>("/admin/sos", { params: { limit } })).data,
    refetchInterval: 5000,
    enabled,
  });
}

export function useConfirmPayment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (rideId: string) =>
      (await api.post<Ride>(`/admin/rides/${rideId}/payments/confirm`)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["rides"] }),
  });
}

export function useUsers(role: Role | "ALL" = "ALL", enabled = true) {
  return useQuery({
    queryKey: ["users", role],
    queryFn: async () => {
      const params: Record<string, string> = {};
      if (role !== "ALL") params.role = role;
      return (await api.get<AdminUser[]>("/admin/users", { params })).data;
    },
    enabled,
  });
}

export function useChangeUserRole() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ userId, role }: { userId: string; role: Role }) =>
      (await api.post<AdminUser>(`/admin/users/${userId}/role`, { role })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });
}

/** Only meaningful while there's no real SMS gateway wired in — see backend LoggingOtpAdapter. */
export function useRecentOtp(enabled = true) {
  return useQuery({
    queryKey: ["otp-recent"],
    queryFn: async () => (await api.get<OtpChallenge[]>("/admin/otp/recent", { params: { limit: 20 } })).data,
    enabled,
    refetchInterval: 5000,
  });
}

export function useDriverEarnings(driverId: string, enabled = true) {
  return useQuery({
    queryKey: ["driver-earnings", driverId],
    queryFn: async () => (await api.get<DriverEarnings>(`/admin/drivers/${driverId}/earnings`)).data,
    enabled: Boolean(driverId) && enabled,
  });
}

export function useDriverActivityStats(driverId: string) {
  return useQuery({
    queryKey: ["driver-stats", driverId],
    queryFn: async () => (await api.get<DriverActivityStats>(`/admin/drivers/${driverId}/stats`)).data,
    enabled: Boolean(driverId),
  });
}

/** Defaults to the last `days` days — pass a wider window to compute period-over-period trends. */
export function useRevenue(days = 14, enabled = true) {
  return useQuery({
    queryKey: ["revenue", days],
    queryFn: async () => {
      const to = new Date();
      const from = new Date(to.getTime() - days * 86_400_000);
      return (
        await api.get<PlatformRevenue>("/admin/revenue", {
          params: { from: from.toISOString(), to: to.toISOString() },
        })
      ).data;
    },
    enabled,
    refetchInterval: 60_000,
  });
}

export function useRevenueByTariff(days = 7) {
  return useQuery({
    queryKey: ["revenue-by-tariff", days],
    queryFn: async () => {
      const to = new Date();
      const from = new Date(to.getTime() - days * 86_400_000);
      return (
        await api.get<TariffRevenuePoint[]>("/admin/revenue/by-tariff", {
          params: { from: from.toISOString(), to: to.toISOString() },
        })
      ).data;
    },
  });
}

export function usePayouts(driverId?: string) {
  return useQuery({
    queryKey: ["payouts", driverId],
    queryFn: async () => {
      const params: Record<string, string> = {};
      if (driverId) params.driverId = driverId;
      return (await api.get<Payout[]>("/admin/payouts", { params })).data;
    },
  });
}

export function useGeneratePayout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: { driverId: string; periodFrom: string; periodTo: string }) =>
      (await api.post<Payout>("/admin/payouts", input)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["payouts"] }),
  });
}

export function useMarkPayoutPaid() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payoutId: string) => (await api.post<Payout>(`/admin/payouts/${payoutId}/pay`)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["payouts"] }),
  });
}

export function useBroadcastHistory(limit = 50) {
  return useQuery({
    queryKey: ["broadcast-history", limit],
    queryFn: async () => (await api.get<BroadcastLogEntry[]>("/admin/broadcast", { params: { limit } })).data,
  });
}

export function useSendBroadcast() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: { segment: BroadcastSegment; title: string; body: string }) =>
      (await api.post<BroadcastLogEntry>("/admin/broadcast", input)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["broadcast-history"] }),
  });
}

export function useAuditLog(limit = 100) {
  return useQuery({
    queryKey: ["audit-log", limit],
    queryFn: async () => (await api.get<AuditLogEntry[]>("/admin/audit-log", { params: { limit } })).data,
    refetchInterval: 10_000,
  });
}

export function useSystemStatus() {
  return useQuery({
    queryKey: ["system-status"],
    queryFn: async () => (await api.get<SystemStatus>("/admin/system-status")).data,
    refetchInterval: 15_000,
  });
}

/** The admin-only detail view — passenger identity + dispatch offer history included, unlike
 * the passenger-facing `GET /rides/{id}`. */
export function useAdminRideDetail(rideId: string) {
  return useQuery({
    queryKey: ["ride", rideId],
    queryFn: async () => (await api.get<AdminRideDetail>(`/admin/rides/${rideId}`)).data,
    enabled: Boolean(rideId),
    refetchInterval: 5000,
  });
}

export function useRideMessages(rideId: string) {
  return useQuery({
    queryKey: ["ride-messages", rideId],
    queryFn: async () => (await api.get<ChatMessage[]>(`/admin/rides/${rideId}/messages`)).data,
    enabled: Boolean(rideId),
    refetchInterval: 5000,
  });
}

export function useForceCancelRide() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ rideId, reason }: { rideId: string; reason?: string }) =>
      (await api.post<Ride>(`/admin/rides/${rideId}/cancel`, { reason })).data,
    onSuccess: (_data, { rideId }) => {
      queryClient.invalidateQueries({ queryKey: ["rides"] });
      queryClient.invalidateQueries({ queryKey: ["ride", rideId] });
    },
  });
}

export function useReassignRide() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ rideId, driverId }: { rideId: string; driverId?: string | null }) =>
      (await api.post<Ride>(`/admin/rides/${rideId}/reassign`, { driverId: driverId ?? null })).data,
    onSuccess: (_data, { rideId }) => {
      queryClient.invalidateQueries({ queryKey: ["rides"] });
      queryClient.invalidateQueries({ queryKey: ["ride", rideId] });
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
    },
  });
}

export function useVerifyDriver() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ driverId, approve, reason }: { driverId: string; approve: boolean; reason?: string }) =>
      api.post(`/admin/drivers/${driverId}/verify`, { approve, reason }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["drivers"] }),
  });
}

/** Fetches a driver's document through the authenticated endpoint and returns a blob URL —
 * documents are sensitive, so they're never served as a public static `<img src>`. */
export function useDriverDocument(driverId: string, kind: DocumentKind, enabled: boolean) {
  return useQuery({
    queryKey: ["driver-document", driverId, kind],
    queryFn: async () => {
      const res = await api.get(`/admin/drivers/${driverId}/documents/${kind}`, { responseType: "blob" });
      return URL.createObjectURL(res.data as Blob);
    },
    enabled,
    staleTime: Infinity,
    gcTime: 0,
  });
}

export function useBanUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ userId, reason }: { userId: string; reason?: string }) =>
      (await api.post<AdminUser>(`/admin/users/${userId}/ban`, { reason })).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
    },
  });
}

export function useUnbanUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (userId: string) => (await api.post<AdminUser>(`/admin/users/${userId}/unban`)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      queryClient.invalidateQueries({ queryKey: ["drivers"] });
    },
  });
}

export function usePromoCodes() {
  return useQuery({
    queryKey: ["promo-codes"],
    queryFn: async () => (await api.get<AdminPromoCode[]>("/admin/promo-codes")).data,
  });
}

export interface PromoCodeInput {
  code: string;
  discountType: DiscountType;
  discountValue: number;
  maxUses?: number | null;
  expiresAt?: string | null;
}

export function useCreatePromoCode() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: PromoCodeInput) => (await api.post<AdminPromoCode>("/admin/promo-codes", input)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["promo-codes"] }),
  });
}

export function useUpdatePromoCode() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, patch }: { id: string; patch: Partial<PromoCodeInput> & { active?: boolean } }) =>
      (await api.patch<AdminPromoCode>(`/admin/promo-codes/${id}`, patch)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["promo-codes"] }),
  });
}
