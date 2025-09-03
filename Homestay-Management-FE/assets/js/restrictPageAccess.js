// assets/js/restrictPageAccess.js

// Hàm kiểm tra token hết hạn (có thể giữ lại hoặc đặt ở đây)
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    const exp = payload.exp;
    if (!exp) return true;
    const now = Math.floor(Date.now() / 1000);
    return exp < now;
  } catch (error) {
    console.error("Token invalid:", error);
    return true;
  }
}

function restrictPageAccess() {
  const token = localStorage.getItem("authToken");
  const user = JSON.parse(localStorage.getItem("user"));
  const roles = user?.roles || [];
  const currentPage = window.location.pathname.split("/").pop(); // Lấy tên file hiện tại

  let requiredRole = null;

  // Xác định role cần thiết cho từng trang
  if (currentPage === "admin-dashboard.html") {
    requiredRole = "ROLE_ADMIN";
  } else if (currentPage === "host-dashboard.html") {
    requiredRole = "ROLE_HOST";
  } else if (currentPage === "my-booking.html") {
    requiredRole = "ROLE_USER";
  }
  // Thêm các trang cần bảo vệ khác nếu có

  // Nếu trang hiện tại không nằm trong danh sách cần bảo vệ, thoát
  if (requiredRole === null) {
     return;
  }

  // --- Logic kiểm tra quyền truy cập ---

  // 1. Kiểm tra token tồn tại và còn hạn
  if (!token || isTokenExpired(token)) {
    console.log("Access denied: No token or token expired");
    localStorage.removeItem("authToken"); // Dọn dẹp token hết hạn
    localStorage.removeItem("user");
    // Chuyển hướng ngay lập tức (dùng replace để người dùng không back lại được)
    window.location.replace("access-denied.html");
    return; // Dừng thực thi script
  }

  // 2. Kiểm tra thông tin user và roles có hợp lệ không (phòng trường hợp user object bị lỗi)
   if (!user || !roles || roles.length === 0) {
     console.log("Access denied: User object missing or no roles");
     window.location.replace("access-denied.html");
     return;
   }


  // 3. Kiểm tra user có role cần thiết hay không
  if (!roles.includes(requiredRole)) {
    console.log(`Access denied: User does not have required role: ${requiredRole}`);
    // Chuyển hướng ngay lập tức
    window.location.replace("access-denied.html");
    return; // Dừng thực thi script
  }

  // Nếu tất cả kiểm tra đều pass, script sẽ tự động thoát và cho phép trang load bình thường
  console.log("Access granted.");
}

// Tự động gọi hàm kiểm tra ngay khi script được thực thi
restrictPageAccess();