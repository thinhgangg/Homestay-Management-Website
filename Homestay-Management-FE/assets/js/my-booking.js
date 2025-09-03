// Hàm render danh sách homestay
function renderHomestays(homestays, containerId, noDataElementId) {
    const container = document.getElementById(containerId);
    const noDataElement = document.getElementById(noDataElementId);

    // Xóa nội dung cũ
    container.innerHTML = "";

    if (homestays.length === 0) {
        noDataElement.style.display = "block";
        return;
    }

    noDataElement.style.display = "none";

    homestays.forEach((homestay) => {
        const homestayCard = document.createElement("div");
        homestayCard.className = "homestay-card";
        homestayCard.innerHTML = `
            <div>
                <h3>${homestay.name}</h3>
                <p><span class="label">Địa chỉ:</span> ${homestay.address}</p>
                <p><span class="label">Giá:</span> ${homestay.price.toLocaleString("vi-VN")} VND</p>
            </div>
            <div>
                <p><span class="label">Ngày Check In:</span> ${homestay.checkIn}</p>
                <p><span class="label">Ngày Check Out:</span> ${homestay.checkOut}</p>
                <p><span class="label">Ngày Đặt:</span> ${homestay.bookingDate}</p>
            </div>
        `;
        container.appendChild(homestayCard);
    });
}

// Dữ liệu mẫu
const homestays = {
    upcoming: [
        {
            name: "Homestay Biển Xanh",
            address: "123 Đường Biển, Đà Nẵng",
            price: 1500000,
            checkIn: "2025-05-10",
            checkOut: "2025-05-12",
            bookingDate: "2025-05-01",
        },
        {
            name: "Nhà Gỗ Núi Rừng",
            address: "45 Đường Rừng, Đà Lạt",
            price: 2000000,
            checkIn: "2025-06-01",
            checkOut: "2025-06-03",
            bookingDate: "2025-04-25",
        },
    ],
    completed: [
        {
            name: "Villa Cát Vàng",
            address: "78 Đường Cát, Phú Quốc",
            price: 3000000,
            checkIn: "2025-04-15",
            checkOut: "2025-04-18",
            bookingDate: "2025-03-20",
        },
    ],
    canceled: [
        {
            name: "Homestay Sông Nước",
            address: "12 Đường Sông, Cần Thơ",
            price: 1200000,
            checkIn: "2025-03-10",
            checkOut: "2025-03-12",
            bookingDate: "2025-02-15",
        },
    ],
};

// Gọi hàm render khi trang tải
document.addEventListener("DOMContentLoaded", () => {
    renderHomestays(homestays.upcoming, "upcoming-list", "no-upcoming");
    renderHomestays(homestays.completed, "completed-list", "no-completed");
    renderHomestays(homestays.canceled, "canceled-list", "no-canceled");
});
