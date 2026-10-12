const $ = id => document.getElementById(id);
const token = localStorage.getItem("token");

if (!token) {
  window.location.replace("login.html");
} else {
  $("welcome").textContent =
    "Welcome, " + (localStorage.getItem("voterId") || "User");

  DigitalVotingAPI.get("/admin/dashboard")
    .then(data => {
      $("welcome").textContent =
        "Welcome, " + data.voterId + " — " + data.message;
      $("roleBadge").textContent = data.role;
    })
    .catch(() => {
      $("globalMessage").textContent =
        "Session invalid or access denied. Please log in again.";
      localStorage.removeItem("token");
      localStorage.removeItem("voterId");
      setTimeout(() => window.location.replace("login.html"), 1200);
    });
}

function showMessage(id, text, success = true) {
  $(id).textContent = text;
  $(id).style.color = success ? "#15803d" : "#b91c1c";
}

function localDateToOffset(id) {
  const value = $(id).value;
  if (!value) return "";
  return new Date(value).toISOString();
}

$("createElectionForm").addEventListener("submit", async event => {
  event.preventDefault();
  const button = event.submitter;
  button.disabled = true;

  try {
    const startTime = localDateToOffset("startTime");
    const endTime = localDateToOffset("endTime");

    if (new Date(endTime) <= new Date(startTime)) {
      throw new Error("End time must be after start time.");
    }

    const data = await DigitalVotingAPI.post("/admin/elections", {
      electionCode: $("electionCode").value.trim(),
      title: $("electionTitle").value.trim(),
      description: $("electionDescription").value.trim(),
      startTime,
      endTime
    });

    showMessage("electionMessage",
      data?.message || "Election created successfully.");
    $("createElectionForm").reset();
  } catch (error) {
    showMessage("electionMessage", error.message, false);
  } finally {
    button.disabled = false;
  }
});

$("statusForm").addEventListener("submit", async event => {
  event.preventDefault();
  const button = event.submitter;
  button.disabled = true;

  try {
    const code = encodeURIComponent($("statusElectionCode").value.trim());
    const data = await DigitalVotingAPI.put(
      `/admin/elections/${code}/status`,
      { status: $("electionStatus").value }
    );

    showMessage("statusMessage",
      data?.message || "Election status updated.");
  } catch (error) {
    showMessage("statusMessage", error.message, false);
  } finally {
    button.disabled = false;
  }
});

$("candidateForm").addEventListener("submit", async event => {
  event.preventDefault();
  const button = event.submitter;
  button.disabled = true;

  try {
    const code = encodeURIComponent(
      $("candidateElectionCode").value.trim()
    );

    const data = await DigitalVotingAPI.post(
      `/admin/elections/${code}/candidates`,
      {
        candidateCode: $("candidateCode").value.trim(),
        candidateName: $("candidateName").value.trim(),
        partyName: $("partyName").value.trim(),
        symbol: $("candidateSymbol").value.trim()
      }
    );

    showMessage("candidateMessage",
      data?.message || "Candidate added successfully.");
    $("candidateForm").reset();
  } catch (error) {
    showMessage("candidateMessage", error.message, false);
  } finally {
    button.disabled = false;
  }
});

$("eligibilityForm").addEventListener("submit", async event => {
  event.preventDefault();
  const button = event.submitter;
  button.disabled = true;

  try {
    const code = encodeURIComponent(
      $("eligibilityElectionCode").value.trim()
    );

    const data = await DigitalVotingAPI.put(
      `/admin/elections/${code}/eligibility`,
      {
        voterId: $("eligibilityVoterId").value.trim(),
        status: $("eligibilityStatus").value
      }
    );

    showMessage("eligibilityMessage",
      data?.message || "Voter eligibility saved.");
  } catch (error) {
    showMessage("eligibilityMessage", error.message, false);
  } finally {
    button.disabled = false;
  }
});

$("logoutButton").addEventListener("click", () => {
  localStorage.removeItem("token");
  localStorage.removeItem("voterId");
  window.location.replace("login.html");
});
