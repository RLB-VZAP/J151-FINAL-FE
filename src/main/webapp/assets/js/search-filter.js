document.addEventListener("DOMContentLoaded", function () {
    // Auto-submit player filters the moment a dropdown changes,
    // so the user doesn't have to also click "Search".
    var clubFilter = document.getElementById("clubFilter");
    var positionFilter = document.getElementById("positionFilter");

    [clubFilter, positionFilter].forEach(function (select) {
        if (select) {
            select.addEventListener("change", function () {
                select.form.submit();
            });
        }
    });
});