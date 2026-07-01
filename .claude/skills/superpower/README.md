# superpowers-skill

> A one-page **Skill** that installs the five-step closing protocol from [*Finally Superpowers*](https://coachsteff.live) by Steff Vanhaverbeke into [Craft Agents](https://craft.do), [Claude Code](https://claude.com/claude-code), or any other SKILL.md-compatible framework.

It is a small, slightly annoying gate. That is the point.

You wrote the rules at your best. Let this file hold them at your worst.

---

## What it does

Before each new conversation with an agent, the **Driver's Seat** skill asks you five questions, waits for your answers, and refuses to proceed until the answers exist:

1. **Intent** — What are you actually trying to achieve?
2. **Thinking mode** — Fast or slow? Be honest.
3. **Scope cap** — How many options, how many iterations?
4. **What stays human** — What must NOT be delegated?
5. **Definition of done** — What does "done" look like, on your terms?

The agent then echoes your five answers back as a single contract and references it for the rest of the session. At the end, it asks one question: *Did we end cleanly, on those terms?*

If you want to skip the gate for a session, just type `drive` or `skip protocol`. The agent will acknowledge it and continue without the gate.

---

## Install in 30 seconds

The folder slug `drivers-seat` matches the skill's `name` and stays the same regardless of the repo name.

### Craft Agents

```bash
mkdir -p ~/.craft-agent/workspaces/<your-workspace>/skills/drivers-seat
cp SKILL.md icon.svg ~/.craft-agent/workspaces/<your-workspace>/skills/drivers-seat/
```

Replace `<your-workspace>` with your workspace ID (e.g. `developer`, `personal`).

### Claude Code

```bash
mkdir -p ~/.claude/skills/drivers-seat
cp SKILL.md ~/.claude/skills/drivers-seat/
```

### Other SKILL.md-compatible tools

Drop `SKILL.md` into your tool's skill directory. The body is plain Markdown — the only frontmatter fields used (`name`, `description`) are part of the shared format.

---

## Why a gate

> Self-leadership tends to evaporate under pressure. The best version of me at nine in the morning is not the version of me at four in the afternoon. A skill does not get tired. A file does not skip steps because today was a hard day.
>
> — *Finally Superpowers*, Conclusion

Install it. Ignore it when you want. Delete it when it no longer serves you. But for the first month of agent work, let it stand between you and the glow. You will notice the difference by the second week.

---

## Remix it

Fork it. Rewrite the questions in your own voice. Translate it into Dutch, French, Spanish. Tighten the contract block. Make the skip word something that fits your team's vocabulary. Whatever helps you actually use it.

If you build something useful on top of this, link back — but the license does not require you to ask permission.

---

## License

[Creative Commons Attribution 4.0 International (CC BY 4.0)](LICENSE)
© 2026 Steff Vanhaverbeke

You are free to share and adapt this material for any purpose, even commercially, as long as you give appropriate credit, link to the license, and indicate if changes were made.

---

## Credits & links

- Book: *Finally Superpowers* — available on Amazon
- Author: [Steff Vanhaverbeke](https://coachsteff.live)
- Coaching: [The House of Coaching](https://thehouseofcoaching.com/en)
- More open-source tools: the **Superworker Toolbox** at [github.com/CoachSteff](https://github.com/CoachSteff)
