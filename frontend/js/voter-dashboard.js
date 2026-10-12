"use strict";

const electionsList = document.getElementById("electionsList");
const pageMessage = document.getElementById("pageMessage");
const candidateSection = document.getElementById("candidateSection");
const candidatesList = document.getElementById("candidatesList");
const candidateMessage = document.getElementById("candidateMessage");
const receiptSection = document.getElementById("receiptSection");

let selectedElection = null;
let selectedCandidate = null;
let submittingVote = false;

function showMessage(element, text, type = "") {
  element.textContent = text;
  element.className = `message ${type}`.trim();
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;",
    '"': "&quot;", "'": "&#39;"
  })[char]);
}

function formatDate(value) {
  if (!value) return "Not specified";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("voterId");
  window.location.href = "login.html";
}

async function loadElections() {
  electionsList.replaceChildren();
  const loading = document.createElement("p");
  loading.textContent = "Loading elections...";
  electionsList.append(loading);
  showMessage(pageMessage, "");

  try {
    const elections = await DigitalVotingAPI.get("/elections");
    electionsList.replaceChildren();

    if (!Array.isArray(elections) || elections.length === 0) {
      const empty = document.createElement("p");
      empty.textContent = "No elections have been created yet.";
      electionsList.append(empty);
      return;
    }

    elections.forEach(election => {
      const card = document.createElement("article");
      card.className = "election-card";

      const title = document.createElement("h3");
      title.textContent = election.title || election.electionCode;

      const description = document.createElement("p");
      description.textContent = election.description || "No description provided.";

      const code = document.createElement("p");
      code.textContent = `Election code: ${election.electionCode}`;

      const status = document.createElement("span");
      status.className = "status";
      status.textContent = election.status || "UNKNOWN";

      const dates = document.createElement("p");
      dates.textContent =
        `Starts: ${formatDate(election.startTime)} | Ends: ${formatDate(election.endTime)}`;

      const button = document.createElement("button");
      button.textContent = "View candidates";
      button.addEventListener("click", () => openElection(election));

      card.append(title, description, code, status, dates, button);
      electionsList.append(card);
    });
  } catch (error) {
    electionsList.replaceChildren();
    showMessage(pageMessage, error.message || "Could not load elections.", "error");
  }
}

async function openElection(election) {
  selectedElection = election;
  selectedCandidate = null;
  candidateSection.hidden = false;
  receiptSection.hidden = true;
  document.getElementById("selectedElectionTitle").textContent =
    `${election.title || election.electionCode} — Candidates`;
  document.getElementById("electionDetails").textContent =
    `Status: ${election.status}. Voting is permitted only when the election is LIVE and your voter account is eligible.`;
  candidatesList.replaceChildren();
  showMessage(candidateMessage, "Loading candidates...");

  try {
    const candidates = await DigitalVotingAPI.get(
      `/elections/${encodeURIComponent(election.electionCode)}/candidates`
    );
    candidatesList.replaceChildren();

    if (!Array.isArray(candidates) || candidates.length === 0) {
      const empty = document.createElement("p");
      empty.textContent = "No candidates found for this election.";
      candidatesList.append(empty);
      showMessage(candidateMessage, "");
      return;
    }

    candidates.forEach(candidate => {
      const card = document.createElement("article");
      card.className = "candidate-card";

      const title = document.createElement("h3");
      title.textContent = candidate.candidateName || candidate.candidateCode;

      const party = document.createElement("p");
      party.textContent = `Party: ${candidate.partyName || "Independent / Not specified"}`;

      const symbol = document.createElement("p");
      symbol.textContent = `Symbol: ${candidate.symbol || "Not specified"}`;

      const code = document.createElement("p");
      code.textContent = `Candidate code: ${candidate.candidateCode}`;

      const status = document.createElement("span");
      status.className = "status";
      status.textContent = candidate.status || "UNKNOWN";

      card.append(title, party, symbol, code, status);

      if (election.status === "LIVE" && candidate.status === "ACTIVE") {
        const selectButton = document.createElement("button");
        selectButton.textContent = "Select candidate";
        selectButton.style.marginTop = "14px";
        selectButton.addEventListener("click", () => {
          selectedCandidate = candidate;
          candidatesList.querySelectorAll(".candidate-card")
            .forEach(item => item.classList.remove("selected"));
          card.classList.add("selected");
          showMessage(
            candidateMessage,
            `Selected: ${candidate.candidateName}. Click Confirm Vote only if this is your final choice.`
          );
          renderConfirmButton();
        });
        card.append(selectButton);
      }

      candidatesList.append(card);
    });

    showMessage(
      candidateMessage,
      election.status === "LIVE"
        ? "Select an active candidate. The server will verify your eligibility."
        : "This election is not LIVE. You can review candidates, but voting is not available."
    );
    renderConfirmButton();
  } catch (error) {
    showMessage(candidateMessage, error.message || "Could not load candidates.", "error");
  }
}

function renderConfirmButton() {
  document.getElementById("confirmVoteBtn")?.remove();
  if (!selectedElection || !selectedCandidate || selectedElection.status !== "LIVE") return;

  const button = document.createElement("button");
  button.id = "confirmVoteBtn";
  button.textContent = "Confirm Vote";
  button.style.margin = "8px 0 24px";
  button.addEventListener("click", castVote);
  candidatesList.after(button);
}

async function castVote() {
  if (submittingVote || !selectedElection || !selectedCandidate) return;

  const confirmed = window.confirm(
    `Confirm your vote for ${selectedCandidate.candidateName} in ${selectedElection.title}? A successful vote cannot be changed.`
  );
  if (!confirmed) return;

  submittingVote = true;
  const button = document.getElementById("confirmVoteBtn");
  if (button) {
    button.disabled = true;
    button.textContent = "Submitting...";
  }
  showMessage(candidateMessage, "Submitting your vote...");

  try {
    const receipt = await DigitalVotingAPI.post("/voter/votes", {
      electionCode: selectedElection.electionCode,
      candidateCode: selectedCandidate.candidateCode
    });

    document.getElementById("receiptMessage").textContent =
      receipt.message || "Vote recorded successfully.";
    document.getElementById("receiptElection").textContent =
      receipt.electionCode || selectedElection.electionCode;
    document.getElementById("receiptCandidate").textContent =
      receipt.candidateCode || selectedCandidate.candidateCode;
    document.getElementById("receiptId").textContent = receipt.receiptId || "Not returned";
    document.getElementById("receiptTime").textContent = formatDate(receipt.votedAt);
    receiptSection.hidden = false;
    showMessage(candidateMessage, "Your vote was submitted successfully.", "success");
    document.getElementById("confirmVoteBtn")?.remove();
    selectedCandidate = null;
    receiptSection.scrollIntoView({ behavior: "smooth", block: "start" });
  } catch (error) {
    showMessage(candidateMessage, error.message || "Vote submission failed.", "error");
    if (button) button.disabled = false;
  } finally {
    submittingVote = false;
    if (button && button.isConnected) button.textContent = "Confirm Vote";
  }
}

document.getElementById("logoutBtn").addEventListener("click", logout);
document.getElementById("refreshBtn").addEventListener("click", loadElections);

async function init() {
  if (!localStorage.getItem("token")) {
    window.location.href = "login.html";
    return;
  }

  document.getElementById("voterLabel").textContent =
    localStorage.getItem("voterId") || "Logged-in voter";

  try {
    const user = await DigitalVotingAPI.get("/voter/dashboard");
    if (user.role !== "ROLE_VOTER") {
      if (user.role === "ROLE_ADMIN") {
        window.location.href = "dashboard.html";
        return;
      }
      throw new Error("This page is available to voter accounts only.");
    }
    await loadElections();
  } catch (error) {
    showMessage(pageMessage, error.message || "Session verification failed.", "error");
    if (/401|403|token|authentication|unauthorized/i.test(error.message || "")) {
      localStorage.removeItem("token");
      window.location.href = "login.html";
    }
  }
}

init();
