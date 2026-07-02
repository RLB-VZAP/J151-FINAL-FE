/* ============================================================================
   TryTons — Fantasy Rugby | Authentication screens behaviour
   Vanilla JS. Progressive enhancement over the existing login/register JSPs.

   HARD CONSTRAINT: this script never changes the auth form contract.
   The real fields (identifier / password / email / username / rawPassword) and
   the submit button (name="submit" value="login|register") are left exactly as
   the JSP renders them. Everything this script adds is presentational chrome
   (hero pane, brand, password toggle, footer) or client-side validation that
   only blocks obviously-invalid submits — the server remains the source of
   truth. All DOM lookups are guarded so the script is inert if markup differs.
   ========================================================================== */
(function () {
  "use strict";

  /* --- tiny helpers ------------------------------------------------------- */
  var SVG_NS = "http://www.w3.org/2000/svg";

  /** Build an element from a tag + props + children. */
  function el(tag, props, children) {
    var node = document.createElement(tag);
    if (props) {
      Object.keys(props).forEach(function (k) {
        if (k === "class") node.className = props[k];
        else if (k === "html") node.innerHTML = props[k];
        else if (k === "text") node.textContent = props[k];
        else if (k in node) node[k] = props[k];
        else node.setAttribute(k, props[k]);
      });
    }
    (children || []).forEach(function (c) {
      node.appendChild(typeof c === "string" ? document.createTextNode(c) : c);
    });
    return node;
  }

  /** Inline-SVG icon factory from raw path markup. */
  function icon(paths, size) {
    var svg =
      '<svg xmlns="' +
      SVG_NS +
      '" viewBox="0 0 24 24" width="' +
      (size || 20) +
      '" height="' +
      (size || 20) +
      '" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round">' +
      paths +
      "</svg>";
    var wrap = document.createElement("span");
    wrap.innerHTML = svg;
    return wrap.firstChild;
  }

  var ICONS = {
    eye:
      '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>',
    eyeOff:
      '<path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c6.5 0 10 7 10 7a17.6 17.6 0 0 1-2.16 3.19"/>' +
      '<path d="M6.61 6.61A17.6 17.6 0 0 0 2 12s3.5 7 10 7a9.12 9.12 0 0 0 4.06-.94"/>' +
      '<path d="M14.12 14.12A3 3 0 1 1 9.88 9.88"/><line x1="2" y1="2" x2="22" y2="22"/>',
    ball:
      '<ellipse cx="12" cy="12" rx="10" ry="6" transform="rotate(45 12 12)"/>' +
      '<line x1="8" y1="12" x2="16" y2="12"/><line x1="10" y1="10" x2="10" y2="14"/>' +
      '<line x1="12" y1="9" x2="12" y2="15"/><line x1="14" y1="10" x2="14" y2="14"/>'
  };

  /* --- page detection (from the real form action; no markup edit) --------- */
  var loginForm = document.querySelector('form[action$="/login"]');
  var registerForm = document.querySelector('form[action$="/register"]');
  var form = loginForm || registerForm || document.querySelector("main form");
  if (!form) return; // not an auth page — do nothing

  var page = loginForm ? "login" : registerForm ? "register" : "auth";
  document.body.setAttribute("data-page", page);

  // Take over client validation: suppress native browser bubbles so our own
  // styled inline errors are shown instead. This only changes DOM behaviour —
  // the field names, action and submit value are untouched, and the server
  // remains the authoritative validator.
  form.noValidate = true;

  var main = document.querySelector("main") || form.parentNode;

  /* ======================================================================
     1. Brand lockup (prepended to <main>)
     ==================================================================== */
  (function buildBrand() {
    if (main.querySelector(".brand")) return;
    var mark = el("span", { class: "brand__mark" }, [icon(ICONS.ball, 22)]);
    var name = el("span", { class: "brand__name", html: "Try<b>Tons</b>" });
    main.insertBefore(el("div", { class: "brand" }, [mark, name]), main.firstChild);
  })();

  /* ======================================================================
     2. Password show/hide toggles
     ==================================================================== */
  Array.prototype.forEach.call(
    form.querySelectorAll('input[type="password"]'),
    function (input) {
      // Wrap the input alone in a relatively-positioned box so the toggle
      // stays vertically centred on the field even when an error message is
      // appended below. The input keeps its id/name — contract untouched.
      var wrap = el("span", { class: "input-wrap" });
      input.parentNode.insertBefore(wrap, input);
      wrap.appendChild(input);
      var btn = el("button", {
        type: "button", // never submits
        class: "pw-toggle",
        "aria-label": "Show password",
        "aria-pressed": "false",
        tabindex: "0"
      });
      btn.appendChild(icon(ICONS.eye, 20));

      btn.addEventListener("click", function () {
        var show = input.type === "password";
        input.type = show ? "text" : "password";
        btn.innerHTML = "";
        btn.appendChild(icon(show ? ICONS.eyeOff : ICONS.eye, 20));
        btn.setAttribute("aria-pressed", show ? "true" : "false");
        btn.setAttribute("aria-label", show ? "Hide password" : "Show password");
        input.focus();
      });

      wrap.appendChild(btn);
    }
  );

  /* ======================================================================
     3. Client-side validation (required + email format).
        Enhances, does not replace, the server-side checks.
     ==================================================================== */
  var EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  function fieldWrap(input) {
    var node = input.parentNode;
    while (node && node.parentNode !== form) node = node.parentNode;
    return node || input.parentNode;
  }

  function clearError(input) {
    var wrap = fieldWrap(input);
    wrap.classList.remove("has-error");
    var msg = wrap.querySelector(".field-error");
    if (msg) msg.remove();
    input.removeAttribute("aria-invalid");
  }

  function setError(input, message) {
    var wrap = fieldWrap(input);
    wrap.classList.add("has-error");
    var msg = wrap.querySelector(".field-error");
    if (!msg) {
      msg = el("span", { class: "field-error", role: "alert" });
      wrap.appendChild(msg);
    }
    msg.textContent = message;
    input.setAttribute("aria-invalid", "true");
  }

  function labelFor(input) {
    var wrap = fieldWrap(input);
    var lbl = wrap.querySelector("label");
    return lbl ? lbl.textContent.trim() : "This field";
  }

  function validateField(input) {
    var value = (input.value || "").trim();
    if (input.hasAttribute("required") && !value) {
      setError(input, labelFor(input) + " is required.");
      return false;
    }
    if (value && (input.type === "email" || input.id === "email")) {
      if (!EMAIL_RE.test(value)) {
        setError(input, "Enter a valid email address.");
        return false;
      }
    }
    clearError(input);
    return true;
  }

  var fields = Array.prototype.slice.call(
    form.querySelectorAll("input")
  );

  fields.forEach(function (input) {
    // Validate on blur; clear the error as the user fixes it.
    input.addEventListener("blur", function () {
      validateField(input);
    });
    input.addEventListener("input", function () {
      if (fieldWrap(input).classList.contains("has-error")) validateField(input);
    });
  });

  form.addEventListener("submit", function (e) {
    var firstBad = null;
    fields.forEach(function (input) {
      if (!validateField(input) && !firstBad) firstBad = input;
    });
    if (firstBad) {
      e.preventDefault();
      firstBad.focus();
    }
  });

  /* ======================================================================
     4. Login-only extras: "keep me signed in" + "forgot password".
        The checkbox is intentionally UNNAMED so it is never posted — it
        does not touch the servlet's parameter contract.
     ==================================================================== */
  if (page === "login") {
    var submitBtn = form.querySelector('button[type="submit"], button[name="submit"]');
    var options = el("div", { class: "auth-options" }, [
      el("label", { class: "auth-remember" }, [
        el("input", { type: "checkbox" }), // no name → not submitted
        document.createTextNode("Keep me signed in")
      ]),
      el("a", { class: "auth-link", href: "#", text: "Forgot password?" })
    ]);
    if (submitBtn) form.insertBefore(options, submitBtn);

    options.querySelector(".auth-link").addEventListener("click", function (ev) {
      ev.preventDefault();
      var id = form.querySelector("#identifier");
      var wrap = id ? fieldWrap(id) : null;
      if (wrap) {
        wrap.classList.remove("has-error");
        var existing = wrap.querySelector(".field-error, .field-hint");
        if (existing) existing.remove();
        var hint = el("span", {
          class: "field-hint",
          role: "status",
          text: "Enter your email above, then contact your league admin to reset."
        });
        wrap.appendChild(hint);
        if (id) id.focus();
      }
    });
  }

  /* ======================================================================
     5. Social sign-in (decorative — no OAuth backend yet).
        type="button" so it can never submit the credentials form.
     ==================================================================== */
  (function buildSocial() {
    var trailing = form.querySelector('button[type="submit"], button[name="submit"]');
    if (!trailing) return;

    var googleIcon =
      '<svg xmlns="' +
      SVG_NS +
      '" viewBox="0 0 24 24" width="18" height="18">' +
      '<path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.76h3.57c2.08-1.92 3.28-4.74 3.28-8.09Z"/>' +
      '<path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.76c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84A11 11 0 0 0 12 23Z"/>' +
      '<path fill="#FBBC05" d="M5.84 14.09a6.6 6.6 0 0 1 0-4.18V7.07H2.18a11 11 0 0 0 0 9.86l3.66-2.84Z"/>' +
      '<path fill="#EA4335" d="M12 4.75c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 1.46 14.97.5 12 .5A11 11 0 0 0 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53Z"/>' +
      "</svg>";

    var divider = el("div", { class: "auth-divider", text: "or" });
    var google = el("button", {
      type: "button",
      class: "btn-google",
      html: googleIcon + "<span>Continue with Google</span>"
    });
    google.addEventListener("click", function () {
      google.disabled = true;
      var span = google.querySelector("span");
      var original = span.textContent;
      span.textContent = "Google sign-in coming soon";
      setTimeout(function () {
        span.textContent = original;
        google.disabled = false;
      }, 1800);
    });

    // Place divider + google directly after the primary CTA.
    if (trailing.nextSibling) {
      form.insertBefore(divider, trailing.nextSibling);
      form.insertBefore(google, divider.nextSibling);
    } else {
      form.appendChild(divider);
      form.appendChild(google);
    }
  })();

  /* ======================================================================
     6. Footer status meta (appended to <main>)
     ==================================================================== */
  (function buildFooter() {
    if (main.querySelector(".auth-footer")) return;
    var status = el("span", { class: "auth-status" }, [
      el("span", { class: "auth-status__dot", "aria-hidden": "true" }),
      document.createTextNode("Live servers operational")
    ]);
    var version = el("span", { text: "v2.4.0-STABLE" });
    main.appendChild(el("div", { class: "auth-footer" }, [status, version]));
  })();

  /* ======================================================================
     7. Hero pane (appended to <body>, right column)
     ==================================================================== */
  (function buildHero() {
    if (document.querySelector(".hero")) return;

    var copy =
      page === "register"
        ? {
            badge: "Season 2026 · Open",
            title: "Draft your <em>dream</em> XV.",
            text:
              "Create your manager account and take command of your roster, transfers and matchday tactics."
          }
        : {
            badge: "Matchday Live",
            title: "Own the <em>breakdown</em>. Rule the league.",
            text:
              "Access your roster and league analytics. Track form, set your lineup and climb the standings."
          };

    var content = el("div", { class: "hero__content" }, [
      el("span", { class: "hero__badge", text: copy.badge }),
      el("h2", { class: "hero__title", html: copy.title }),
      el("p", { class: "hero__text", text: copy.text }),
      el("div", { class: "hero__stats" }, [
        el("div", { class: "hero__stat", html: "<b>128</b><span>Managers</span>" }),
        el("div", { class: "hero__stat", html: "<b>16</b><span>Clubs</span>" }),
        el("div", { class: "hero__stat", html: "<b>Live</b><span>Analytics</span>" })
      ])
    ]);

    var hero = el("aside", { class: "hero", "aria-hidden": "true" }, [content]);
    document.body.appendChild(hero);
  })();
})();
