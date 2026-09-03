# Vein Surveyor v1.2.0 — Fabric 1.21.11

Vein Surveyor is a client-side geological surveying and 3D trajectory analysis mod for Minecraft Fabric 1.21.11, tailored for the linear ore generation mechanics on **CivNodes** (`play.civnodes.net`) and similar servers.

---

### 🌟 What's New in v1.2.0

- **3D PCA Vein Trajectory Fitting:** Extracts the 3D strike vector $\vec{v}$, centroid, pitch/yaw compass heading, and $R^2$ fit confidence from logged coordinates.
- **50m Auto-Detect Mode (`NUMPAD 3`):** Sweeps client chunks within a 50-meter radius to automatically import exposed diamond ore blocks, enabling instant reconstruction of previously mined tunnels.
- **Multi-Vein Concurrent Sessions:** Track independent veins simultaneously (`Vein 1`, `Vein 2`, `Vein 3`) with dedicated point lists and cached analytics.
- **Gap & Density Cutoff Guidance:** 
  - Calculates true linear yield ($\rho = \frac{N - 1}{L}$) and average spacing ($\mu_{\text{gap}}$).
  - Multi-zone in-world visualization: **Gold** (Core) $\rightarrow$ **Neon Green** (High-Confidence) $\rightarrow$ **Orange** (Thinning) $\rightarrow$ **Red** (Over-Tunneling).
  - **Red Octagon Termination Stop Marker** rendered at $+3.5\times \mu_{\text{gap}}$.
- **Excavation Profile Recommendations:** Computes perpendicular scatter ($\pm R$) to suggest optimal tunnel dimensions (`Dig 2x2 tunnel`, `Dig 3x3 tunnel`).
- **Numpad Default Controls:** Fully customizable in **Options $\rightarrow$ Controls $\rightarrow$ Key Binds $\rightarrow$ Vein Surveyor**.

---

### ⌨️ Default Keybindings (Numpad)

| Key | Action | Description |
|:---|:---|:---|
| **`NUMPAD 0`** | **Survey Block** | Tags the targeted ore block into the active session. |
| **`NUMPAD 1`** | **Undo Point** | Removes the last recorded point from the active session. |
| **`NUMPAD 2`** | **Clear Active Vein** | Clears all points in the active session. |
| **`NUMPAD 3`** | **Toggle Auto-Scan** | Toggles continuous 50m auto-detection of exposed diamond ores. |
| **`NUMPAD 4`** | **New Session** | Starts a new vein session (`Vein 2`, `Vein 3`, etc.). |
| **`NUMPAD 5`** | **Delete Session** | Deletes the active session and selects the adjacent one. |
| **`NUMPAD 6`** | **Cycle Session** | Cycles which vein is actively surveyed and rendered. |
| **`NUMPAD 7`** | **Toggle HUD** | Toggles the statistical HUD overlay. |
| **`NUMPAD 8`** | **Cycle Reach** | Cycles projection reach (`25m` $\rightarrow$ `50m` $\rightarrow$ `100m` $\rightarrow$ `200m`). |

---

### 📋 Installation & Requirements

1. Install **Fabric Loader** (0.16.x or newer) for **Minecraft 1.21.11**.
2. Install **Fabric API**.
3. Place `veinsurveyor-1.2.0.jar` in your `.minecraft/mods` folder.
4. Launch and start surveying!

---

### ⚖️ Fair Play & Server Approval
- **CivNodes (`play.civnodes.net`):** Officially reviewed and approved by staff.
- **Client-Side Only:** No packets sent or modified. Only accesses exposed blocks in client-loaded chunks.
