/* ============================================================================
   Players page — live filter + sort.
   The servlet renders the whole roster; search, club, position and sort all
   recompute the visible list here, so nothing round-trips to the server.
   ========================================================================== */
document.addEventListener("DOMContentLoaded", function () {
    var searchInput = document.getElementById("playerSearchInput");
    var clubFilter = document.getElementById("clubFilter");
    var positionFilter = document.getElementById("positionFilter");
    var sortSelect = document.getElementById("playerSort");
    var table = document.getElementById("playersTable");
    var body = document.getElementById("playersBody");
    var emptyState = document.getElementById("playersEmptyState");
    var countLabel = document.getElementById("playersCount");

    if (!body) {
        return;
    }

    var rows = Array.prototype.slice.call(body.querySelectorAll(".prow"));

    function matches(row) {
        var term = searchInput ? searchInput.value.trim().toLowerCase() : "";
        if (term && row.dataset.name.indexOf(term) === -1) {
            return false;
        }
        if (clubFilter && clubFilter.value && row.dataset.club !== clubFilter.value) {
            return false;
        }
        if (positionFilter && positionFilter.value && row.dataset.position !== positionFilter.value) {
            return false;
        }
        return true;
    }

    // Name and club read best A-Z; value and form are "who's best" questions,
    // so they lead with the highest.
    function compare(a, b) {
        var mode = sortSelect ? sortSelect.value : "name";
        if (mode === "club") {
            return a.dataset.clubName.localeCompare(b.dataset.clubName);
        }
        if (mode === "value") {
            return parseFloat(b.dataset.value) - parseFloat(a.dataset.value);
        }
        if (mode === "form") {
            return parseFloat(b.dataset.form) - parseFloat(a.dataset.form);
        }
        return a.dataset.name.localeCompare(b.dataset.name);
    }

    function apply() {
        var visible = rows.filter(matches);
        visible.sort(compare);

        rows.forEach(function (row) {
            row.hidden = true;
        });
        visible.forEach(function (row) {
            row.hidden = false;
            body.appendChild(row);
        });

        if (table) {
            table.hidden = visible.length === 0;
        }
        if (emptyState) {
            emptyState.hidden = visible.length !== 0;
        }
        if (countLabel) {
            countLabel.textContent = visible.length + (visible.length === 1 ? " player" : " players") + " · season 2025/26";
        }
    }

    if (searchInput) {
        searchInput.addEventListener("input", apply);
    }
    [clubFilter, positionFilter, sortSelect].forEach(function (control) {
        if (control) {
            control.addEventListener("change", apply);
        }
    });

    apply();
});
