import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeAll, describe, expect, it } from "vitest";

import App from "./App";

beforeAll(() => {
  Object.defineProperty(window, "matchMedia", {
    writable: true,
    value: () => ({
      matches: false,
      addEventListener: () => undefined,
      removeEventListener: () => undefined,
    }),
  });
});

afterEach(cleanup);

describe("Mission Control execution layer", () => {
  it("renders the shared action plan, roadmap, health, history, and scenarios", async () => {
    render(<App />);

    expect(await screen.findByText("Your dynamic action plan")).toBeTruthy();
    expect(screen.getByText("Mission health")).toBeTruthy();
    expect(screen.getByText("Readiness over time")).toBeTruthy();
    expect(screen.getByText("Evaluate the decision")).toBeTruthy();
    expect(screen.getByText("30 Days")).toBeTruthy();
    expect(screen.getByText("90 Days")).toBeTruthy();
    expect(screen.getByText("1 Year")).toBeTruthy();
    expect(screen.getByText("Your mission briefing")).toBeTruthy();
    expect(screen.getByText("Claude Mission Coach")).toBeTruthy();
  });

  it("switches Claude explanations and asks a mission-specific question", async () => {
    render(<App />);

    const noAction = await screen.findByRole("button", {
      name: "What if I do nothing?",
    });
    fireEvent.click(noAction);

    expect(screen.getByText(/remains unrealized/)).toBeTruthy();

    fireEvent.click(screen.getByRole("button", { name: "Can I buy this home?" }));
    expect(screen.getByRole("dialog", { name: "Move the mission forward" })).toBeTruthy();
    expect(screen.getByText("Can I realistically buy this home?")).toBeTruthy();
  });

  it("opens the local mission notification center", async () => {
    render(<App />);
    const notifications = await screen.findByRole("button", {
      name: "Mission notifications",
    });

    fireEvent.click(notifications);

    expect(screen.getByRole("heading", { name: "What changed" })).toBeTruthy();
    expect(screen.getAllByText("New action unlocked").length).toBeGreaterThan(0);
  });

  it("completes an unlocked action and refreshes mission progress", async () => {
    render(<App />);
    const complete = await screen.findByRole("button", {
      name: /Mark action complete/,
    });

    fireEvent.click(complete);

    expect((await screen.findAllByText("Completed")).length).toBeGreaterThan(0);
  });
});
