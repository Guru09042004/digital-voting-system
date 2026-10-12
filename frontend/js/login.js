const loginForm = document.getElementById("loginForm");

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const message = document.getElementById("message");
  const button = loginForm.querySelector("button[type='submit']");
  const voterId = document.getElementById("voterId").value.trim();
  const password = document.getElementById("password").value;

  button.disabled = true;
  button.textContent = "Signing in...";
  message.textContent = "";

  try {
    const data = await DigitalVotingAPI.post("/login", {
      voterId,
      password
    });

    if (!data || !data.token) {
      throw new Error("Login response did not contain a token.");
    }

    localStorage.setItem("token", data.token);
    localStorage.setItem("voterId", voterId);

    try {
      await DigitalVotingAPI.get("/admin/dashboard");
      window.location.replace("dashboard.html");
      return;
    } catch (error) {
      // Admin access nahi mila; ab voter access check karenge.
    }

    try {
      await DigitalVotingAPI.get("/voter/dashboard");
      window.location.replace("voter-dashboard.html");
      return;
    } catch (error) {
      localStorage.removeItem("token");
      localStorage.removeItem("voterId");
      throw new Error(
        "Dashboard access nahi mila. Account role aur verification status check karo."
      );
    }
  } catch (error) {
    message.textContent = error.message || "Login failed.";
    message.style.color = "red";
  } finally {
    button.disabled = false;
    button.textContent = "Login";
  }
});
