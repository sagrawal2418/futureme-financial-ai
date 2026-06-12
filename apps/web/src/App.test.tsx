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
  Object.defineProperty(window, "scrollTo", {
    writable: true,
    value: () => undefined,
  });
});

afterEach(cleanup);

describe("simplified FutureMe product architecture", () => {
  it("starts with one highest-impact action and four supporting signals", async () => {
    render(<App />);

    expect(await screen.findByText("Your highest-impact action")).toBeTruthy();
    expect(screen.getByText("Readiness change")).toBeTruthy();
    expect(screen.getByText("Biggest risk")).toBeTruthy();
    expect(screen.getByText("Biggest opportunity")).toBeTruthy();
    expect(screen.getByText("What you are preparing for")).toBeTruthy();
  });

  it("uses the five-tab customer-question navigation", async () => {
    render(<App />);

    expect(await screen.findByRole("button", { name: /Home/ })).toBeTruthy();
    expect(screen.getByRole("button", { name: /Missions/ })).toBeTruthy();
    expect(screen.getByRole("button", { name: /Insights/ })).toBeTruthy();
    expect(screen.getByRole("button", { name: /Coach/ })).toBeTruthy();
    expect(screen.getByRole("button", { name: /Profile/ })).toBeTruthy();
  });

  it("keeps mission execution actionable without exposing another dashboard", async () => {
    render(<App />);

    fireEvent.click(await screen.findByRole("button", { name: /Missions/ }));

    expect(screen.getByText("One sequence, not another dashboard")).toBeTruthy();
    expect(screen.getByText("Biggest blocker")).toBeTruthy();
    expect(screen.getByRole("button", { name: /Mark complete/ })).toBeTruthy();
  });

  it("shows the 50-prompt AI quality benchmark in Coach", async () => {
    render(<App />);

    fireEvent.click(await screen.findByRole("button", { name: /Coach/ }));

    expect(screen.getByText("AI evaluation dashboard")).toBeTruthy();
    expect(screen.getByText("50 realistic prompts")).toBeTruthy();
    expect(screen.getByText("50-prompt quality benchmark ready to run")).toBeTruthy();
  });

  it("shows five realistic customer journeys and executive demo mode", async () => {
    render(<App />);

    fireEvent.click(await screen.findByRole("button", { name: /Profile/ }));

    expect(screen.getAllByText("Young Family").length).toBeGreaterThan(0);
    expect(screen.getByText("High Income Professional")).toBeTruthy();
    expect(screen.getByText("Pre-Retirement Couple")).toBeTruthy();
    expect(screen.getByText("New Home Buyer")).toBeTruthy();
    expect(screen.getByText("Single Parent")).toBeTruthy();
    expect(screen.getByText("Executive demo mode")).toBeTruthy();
  });
});
