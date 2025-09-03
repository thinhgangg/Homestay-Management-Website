// Toggle mobile menu
document.addEventListener("DOMContentLoaded", function () {
    const menuToggle = document.querySelector(".mobile-menu-toggle");
    const mobileMenu = document.querySelector(".mobile-menu");

    if (menuToggle && mobileMenu) {
        menuToggle.addEventListener("click", function () {
            mobileMenu.classList.toggle("active");

            const isExpanded = mobileMenu.classList.contains("active");
            menuToggle.setAttribute("aria-expanded", isExpanded);

            const icon = menuToggle.querySelector("i");
            if (isExpanded) {
                icon.classList.remove("ti-menu");
                icon.classList.add("ti-close");
            } else {
                icon.classList.remove("ti-close");
                icon.classList.add("ti-menu");
            }
        });
    }

    document.addEventListener("click", function (event) {
        if (mobileMenu && mobileMenu.classList.contains("active") && !mobileMenu.contains(event.target) && !menuToggle.contains(event.target)) {
            closeMobileMenu();
        }
    });
});

function closeMobileMenu() {
    const mobileMenu = document.querySelector(".mobile-menu");
    const menuToggle = document.querySelector(".mobile-menu-toggle");
    if (mobileMenu && menuToggle && mobileMenu.classList.contains("active")) {
        mobileMenu.classList.remove("active");
        menuToggle.setAttribute("aria-expanded", "false");
        const icon = menuToggle.querySelector("i");
        icon.classList.remove("ti-close");
        icon.classList.add("ti-menu");
    }
}
