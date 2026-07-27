# Fantasy TryTons Team Coding Conventions

These conventions are followed across the project for consistency, readability, maintainability and professional code quality.

## The 20 Rules

1. **Avoid unnecessary multiline code** — Keep a single logical statement on one line where it stays readable. Only split when it significantly improves readability.
   - Example: prefer `return fantasyTeamDAO.findById(teamId).orElseThrow(() -> new EntityNotFoundException("Fantasy team not found."));` over the same split across 5 lines.

2. **One responsibility per method** — A method does one well-defined task. Avoid a registerUser() that validates, hashes, saves, emails and audits all inline; extract validateUser(), createUser(), sendRegistrationEmail().

3. **Keep methods short** — Generally 20-40 lines. If it needs excessive scrolling, extract helpers.

4. **Use descriptive method names** — calculateFantasyPoints(), validateSquad(), createFantasyTeam(), findPlayerById(). Avoid calc(), handle(), doThing(), process().

5. **Use meaningful variable names** — remainingBudget, selectedPlayers, captainBonus, leagueManager. Avoid x, temp, obj, data.

6. **Never use magic numbers** — Declare constants in UPPER_SNAKE_CASE. Prefer `private static final int TEAM_SIZE = 20; if (players.size() != TEAM_SIZE)` over `if (players.size() != 20)`. Examples: TEAM_SIZE, MAX_PLAYERS, BCRYPT_WORK_FACTOR.

7. **Validate early with guard clauses** — Exit invalid conditions immediately instead of nesting.

8. **Avoid deep nesting** — Return early rather than stacking if statements.

9. **Use Optional properly** — Prefer `dao.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found."))` over manual isPresent()/get().

10. **Throw specific exceptions** — IllegalArgumentException("Password cannot be blank."), EntityNotFoundException("League not found."), IllegalStateException("Transfer window has closed."). Never bare `new Exception()`.

11. **Minimise comments** — Good code explains itself through naming. Comment WHY, not WHAT. Acceptable: `// BCrypt ignores characters beyond 72 bytes.` Avoid: `// Increment i`.

12. **Follow layer separation** — A table:
    - Resource handles HTTP requests/responses
    - Service holds business logic
    - DAO does database access
    - DTO transfers data
    - Model is domain objects
    - Business logic never lives in Resources or DAOs.

13. **No SQL outside DAOs** — SQL belongs exclusively in DAO implementations. Prefer playerDAO.findAllPlayers() over a service running raw SQL.

14. **Keep Resources thin** — Delegate immediately to Services. Example: `@GET public Response getPlayer(UUID id) { return Response.ok(playerService.getPlayer(id)).build(); }`

15. **Services own business logic** — Calculations, validation, permissions and workflow live in Service classes.

16. **Use DTOs between layers** — Never expose database entities to the frontend. Flow: Resource -> RequestDTO -> Service -> DAO -> Model, returning Model -> ResponseDTO -> Resource.

17. **Keep formatting consistent** — Opening brace on the same line, four-space indentation, one blank line between logical sections, no trailing whitespace, consistent spacing around operators.

18. **Prefer constants over repeated strings** — E.g. `private static final String ROLE_ADMIN = "ADMIN";` instead of repeating the literal.

19. **Fail fast** — Validate IDs, permissions, squad size and budget before touching the database.

20. **Keep code readable first** — Readable beats clever. Avoid nested ternaries like `return a ? b ? c : d : e;`.

## Summary

This project prioritises:

- **Readability over cleverness** — Simple, clear code is always preferred.
- **Small focused methods** — Each method has a single, well-defined responsibility.
- **Descriptive names** — Variable and method names clearly express intent.
- **Early validation** — Guard clauses and fail-fast principles prevent invalid state.
- **Minimal nesting** — Early returns and simple control flow improve readability.
- **Minimal comments** — Code structure and naming should be self-explanatory; comments explain why, not what.
- **Strong separation of concerns** — Clear boundaries between Resources, Services, DAOs, Models and DTOs.
- **Consistent formatting** — Uniform style across the codebase supports maintainability.
- **Meaningful exceptions** — Specific, informative exception types aid debugging and error handling.
- **Maintainable professional code** — Code written for others to read, understand, and modify.
