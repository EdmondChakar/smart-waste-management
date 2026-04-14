export const APP_ROUTES = {
  adminLogin: "/admin/login",
  adminSignup: "/admin/signup",
  adminDashboard: "/admin/dashboard",
  adminBins: "/admin/bins",
  adminMap: "/admin/map",
  adminUsers: "/admin/users",
  adminDevices: "/admin/devices",
  adminBinDetailPattern: "/admin/bins/:binId"
};

export function getAdminBinDetailRoute(binId) {
  return `/admin/bins/${binId}`;
}
