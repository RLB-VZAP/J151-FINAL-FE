/* ============================================================================
   TryTons — Fantasy Rugby | Admin Match Result Capture behaviour
   (W3-FE-ADMINMATCHRESULT-01C)
   Vanilla JS. Progressive enhancement over pages/admin-match-results.jsp.

   This script never changes the form contract: the posted fields
   (action, fixtureId, teamAScore, teamBScore, and the stat counts) are
   exactly what the JSP renders and what AdminMatchResultServlet reads. The
   server remains the source of truth — the winner preview below is UX only,
   and fantasy points are never derived or shown here. Every lookup is guarded
   so the script stays inert when the forms are not on the page.
   ========================================================================== */
(function () {
  "use strict";

  var resultForm = document.getElementById("matchResultForm");
  if (!resultForm) return; // no fixture selected / different page — do nothing

  var teamAScore = document.getElementById("teamAScore");
  var teamBScore = document.getElementById("teamBScore");
  var preview = document.getElementById("scorePreview");

  /* Winner preview — mirrors how the backend reads the scores, display only. */
  function refreshPreview() {
    if (!preview || !teamAScore || !teamBScore) return;

    var a = parseInt(teamAScore.value, 10);
    var b = parseInt(teamBScore.value, 10);

    if (isNaN(a) || isNaN(b)) {
      preview.textContent = "";
      return;
    }

    if (a === b) {
      preview.textContent = "Preview: draw at " + a + " all.";
    } else if (a > b) {
      preview.textContent = "Preview: Team A leads " + a + " - " + b + ".";
    } else {
      preview.textContent = "Preview: Team B leads " + b + " - " + a + ".";
    }
  }

  if (teamAScore) teamAScore.addEventListener("input", refreshPreview);
  if (teamBScore) teamBScore.addEventListener("input", refreshPreview);
  refreshPreview();

  /* Guard against negative counts client-side; the servlet re-validates. */
  var counts = document.querySelectorAll('#statCounts input[type="number"]');
  Array.prototype.forEach.call(counts, function (input) {
    input.addEventListener("change", function () {
      if (input.value !== "" && parseInt(input.value, 10) < 0) {
        input.value = "0";
      }
    });
  });
})();
