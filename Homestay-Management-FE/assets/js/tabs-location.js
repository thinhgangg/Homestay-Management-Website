// assets/js/tabs-location.js
function setupTabsLocation() {
    const tabs = document.querySelectorAll(".tab");
    const propertyGrids = document.querySelectorAll(".property-grid");
    const viewMoreText = document.getElementById("view-more-text");

    tabs.forEach((tab) => {
        tab.addEventListener("click", function () {
            tabs.forEach((t) => t.classList.remove("active"));
            this.classList.add("active");

            let selectedLocation = this.getAttribute("data-location");

            propertyGrids.forEach((grid) => {
                if (grid.getAttribute("data-location") === selectedLocation) {
                    grid.style.display = "grid";
                } else {
                    grid.style.display = "none";
                }
            });

            viewMoreText.textContent = this.textContent;
        });
    });
}

export { setupTabsLocation };
