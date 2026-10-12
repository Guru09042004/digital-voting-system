const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const voterId = document.getElementById("voterId").value.trim();
        const fullName = document.getElementById("name").value.trim();
        const dateOfBirth = document.getElementById("dob").value;
        const mobile = document.getElementById("mobile").value.trim();
        const email = document.getElementById("email").value.trim();

        const password = document.getElementById("password").value;
        const confirmPassword =
            document.getElementById("confirmPassword").value;

        const message = document.getElementById("message");

        // Password match validation
        if (password !== confirmPassword) {
            message.textContent = "Passwords do not match.";
            message.style.color = "red";
            return;
        }

        // Password length validation
        if (password.length < 8) {
            message.textContent =
                "Password must contain at least 8 characters.";
            message.style.color = "red";
            return;
        }

        // Mobile validation
        if (!/^[0-9]{10}$/.test(mobile)) {
            message.textContent =
                "Please enter a valid 10 digit mobile number.";
            message.style.color = "red";
            return;
        }

        // Disable button while request is running
        const submitButton = registerForm.querySelector("button[type='submit']");
        submitButton.disabled = true;
        submitButton.textContent = "Registering...";

        try {

            const response = await fetch(
                "http://localhost:8082/api/register",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        voterId: voterId,
                        fullName: fullName,
                        dateOfBirth: dateOfBirth,
                        mobile: mobile,
                        email: email,
                        password: password
                    })
                }
            );

            const data = await response.json();

            if (response.ok) {

                message.textContent = data.message;
                message.style.color = "green";

                registerForm.reset();

                console.log("Registration successful:", data);

            } else {

                message.textContent =
                    data.message || "Registration failed.";

                message.style.color = "red";

                console.error("Registration failed:", data);
            }

        } catch (error) {

            console.error("Backend connection error:", error);

            message.textContent =
                "Unable to connect to backend. Please try again.";

            message.style.color = "red";

        } finally {

            submitButton.disabled = false;
            submitButton.textContent = "Register";
        }

    });
}
