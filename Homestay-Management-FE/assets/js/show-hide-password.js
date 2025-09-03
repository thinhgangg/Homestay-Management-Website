// show-hide-password.js
function showHidePassword() {
    const showHideButtons = document.querySelectorAll(".show-hide-btn");
    const passwordInputs = document.querySelectorAll("#registerPassword, #confirmPassword, #loginPassword");

    showHideButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const isPasswordVisible = passwordInputs[0].type === "password";
            passwordInputs.forEach((input) => {
                input.type = isPasswordVisible ? "text" : "password";
            });
            showHideButtons.forEach((btn) => {
                btn.src = isPasswordVisible ? "/assets/img/icon/eye-crossed.svg" : "/assets/img/icon/eye.svg";
                btn.alt = isPasswordVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu";
            });
        });
    });
}

export { showHidePassword };
