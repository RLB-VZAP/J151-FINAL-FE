/* Conversation polling. */
(function () {
    "use strict";

    var container = document.getElementById("conversationMessages");
    if (!container) {
        return;
    }

    var contextPath = container.getAttribute("data-context-path") || "";
    var counterpart = container.getAttribute("data-counterpart");
    var POLL_INTERVAL_MS = 4000;

    function lastCreatedAt() {
        var bubbles = container.querySelectorAll(".msg-bubble");
        if (!bubbles.length) {
            return null;
        }
        return bubbles[bubbles.length - 1].getAttribute("data-created-at");
    }

    function scrollToBottom() {
        container.scrollTop = container.scrollHeight;
    }

    function clearPlaceholder() {
        var placeholder = container.querySelector("#noMessagesYetState");
        if (placeholder) {
            placeholder.remove();
        }
    }

    function appendMessage(message) {
        clearPlaceholder();
        var bubble = document.createElement("div");
        bubble.className = "msg-bubble " + (message.mine ? "is-mine" : "is-theirs");
        bubble.setAttribute("data-created-at", message.createdAt);

        var body = document.createElement("p");
        body.className = "msg-bubble-body";
        body.textContent = message.body;

        var time = document.createElement("span");
        time.className = "msg-bubble-time";
        time.textContent = String(message.createdAt).replace("T", " ").slice(0, 16);

        bubble.appendChild(body);
        bubble.appendChild(time);
        container.appendChild(bubble);
    }

    function poll() {
        var since = lastCreatedAt();
        var url = contextPath + "/messages?format=json&with=" + encodeURIComponent(counterpart);
        if (since) {
            url += "&since=" + encodeURIComponent(since);
        }

        fetch(url, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
            .then(function (response) {
                if (response.status === 401) {
                    window.location.href = contextPath + "/login";
                    return null;
                }
                if (!response.ok) {
                    return null;
                }
                return response.json();
            })
            .then(function (messages) {
                if (!messages || !messages.length) {
                    return;
                }
                messages.forEach(appendMessage);
                scrollToBottom();
            })
            .catch(function () {
                /* transient network error: ignore and retry on next tick */
            });
    }

    scrollToBottom();
    setInterval(poll, POLL_INTERVAL_MS);
})();

/*
 * Type-ahead on the New message tab. The server sends the tab down with no
 * contacts at all; results appear only once something is typed, and narrow with
 * each further keystroke. Rows are built here rather than fetched as HTML, so
 * every value goes in through textContent or setAttribute — never innerHTML.
 */
(function () {
    "use strict";

    var form = document.getElementById("contactSearchForm");
    var input = document.getElementById("contactSearch");
    var results = document.getElementById("contactResults");
    if (!form || !input || !results) {
        return;
    }

    var contextPath = form.getAttribute("data-context-path") || "";
    var csrfToken = form.getAttribute("data-csrf") || "";
    var DEBOUNCE_MS = 180;

    var timer = null;
    var inFlight = null;
    // Responses can land out of order; only the newest term may paint.
    var latestTerm = null;

    // The button is the no-JavaScript fallback. Searching is live now, so it
    // would only ever re-submit what is already on screen.
    var button = document.getElementById("contactSearchButton");
    if (button) {
        button.hidden = true;
    }
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        schedule(0);
    });

    function el(tag, className, text) {
        var node = document.createElement(tag);
        if (className) {
            node.className = className;
        }
        if (text !== undefined && text !== null) {
            node.textContent = text;
        }
        return node;
    }

    function hidden(name, value) {
        var field = document.createElement("input");
        field.type = "hidden";
        field.name = name;
        field.value = value;
        return field;
    }

    function postForm(className) {
        var postTo = document.createElement("form");
        postTo.method = "post";
        postTo.action = contextPath + "/messages";
        if (className) {
            postTo.className = className;
        }
        postTo.appendChild(hidden("csrfToken", csrfToken));
        return postTo;
    }

    function showMessage(text) {
        results.textContent = "";
        var empty = el("p", "msg-empty", text);
        empty.id = "contactsEmptyState";
        results.appendChild(empty);
    }

    /* One branch per MessageContactState, matching messages.jsp. */
    function actionFor(contact) {
        if (contact.state === "ACCEPTED") {
            var open = el("a", "msg-ghost msg-btn-sm", "Open chat");
            open.href = contextPath + "/messages?with=" + encodeURIComponent(contact.userId)
                + "&name=" + encodeURIComponent(contact.username);
            return open;
        }
        if (contact.state === "REQUEST_SENT") {
            return el("span", "msg-state msg-state-pending", "Request sent");
        }
        if (contact.state === "REQUEST_RECEIVED") {
            var accept = postForm(null);
            accept.appendChild(hidden("action", "acceptRequest"));
            accept.appendChild(hidden("requestId", contact.requestId));
            var acceptButton = el("button", "btn-gold msg-btn-sm", "Accept their request");
            acceptButton.type = "submit";
            accept.appendChild(acceptButton);
            return accept;
        }
        if (contact.state === "BLOCKED") {
            return el("span", "msg-state msg-state-blocked", "Blocked");
        }

        /* NONE or DECLINED: asking again is allowed. */
        var request = postForm("msg-request-form");
        request.appendChild(hidden("action", "requestAccess"));
        request.appendChild(hidden("addresseeUserId", contact.userId));

        var noteId = "intro-" + contact.userId;
        var label = el("label", "msg-visually-hidden", "Note");
        label.htmlFor = noteId;

        var note = document.createElement("input");
        note.className = "msg-input msg-input-sm";
        note.type = "text";
        note.id = noteId;
        note.name = "introMessage";
        note.maxLength = 255;
        note.placeholder = "Optional note";

        var send = el("button", "btn-gold msg-btn-sm",
            contact.state === "DECLINED" ? "Ask again" : "Request");
        send.type = "submit";

        request.appendChild(label);
        request.appendChild(note);
        request.appendChild(send);
        return request;
    }

    function render(contacts, term) {
        if (!contacts.length) {
            showMessage("No users match “" + term + "”.");
            return;
        }

        var list = el("ul", "msg-contact-list");
        list.id = "contactsList";
        contacts.forEach(function (contact) {
            var row = el("li", "msg-contact");
            row.appendChild(el("span", "msg-avatar",
                (contact.username || "?").charAt(0).toUpperCase()));
            row.appendChild(el("span", "msg-contact-name", contact.username));
            row.appendChild(actionFor(contact));
            list.appendChild(row);
        });

        results.textContent = "";
        results.appendChild(list);
    }

    function search(term) {
        latestTerm = term;
        if (inFlight) {
            inFlight.abort();
        }
        inFlight = new AbortController();

        fetch(contextPath + "/messages?format=json&contacts=1&q=" + encodeURIComponent(term), {
            headers: { "Accept": "application/json" },
            credentials: "same-origin",
            signal: inFlight.signal
        })
            .then(function (response) {
                if (response.status === 401) {
                    window.location.href = contextPath + "/login";
                    return null;
                }
                if (!response.ok) {
                    return null;
                }
                return response.json();
            })
            .then(function (contacts) {
                if (term !== latestTerm) {
                    return;
                }
                if (!contacts) {
                    showMessage("Unable to search for users right now.");
                    return;
                }
                render(contacts, term);
            })
            .catch(function (error) {
                // An abort is this code superseding its own request, not a failure.
                if (error && error.name !== "AbortError" && term === latestTerm) {
                    showMessage("Unable to search for users right now.");
                }
            });
    }

    function schedule(delay) {
        window.clearTimeout(timer);
        var term = input.value.trim();
        if (!term) {
            latestTerm = null;
            if (inFlight) {
                inFlight.abort();
                inFlight = null;
            }
            showMessage("Type a username or email above to find someone to message.");
            return;
        }
        timer = window.setTimeout(function () {
            search(term);
        }, delay);
    }

    input.addEventListener("input", function () {
        schedule(DEBOUNCE_MS);
    });

    // Coming back with ?q= already filled (the no-JavaScript path, or the back
    // button) leaves a server-rendered list in place — nothing to redraw.
    input.focus();
})();
