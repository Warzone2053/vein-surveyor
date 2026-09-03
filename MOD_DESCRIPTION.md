# Vein Surveyor

> [!NOTE]
> **Server Approval Status:**
> - **CivNodes (`play.civnodes.net`):** Officially reviewed and **approved** by the staff team.
> - **Other Servers:** Different multiplayer servers enforce varying rules regarding HUD overlays and visual survey guidelines. Please check with your server's administration or mod-approval team before using Vein Surveyor on any server not explicitly listed above.

**Vein Surveyor** is a client-side Fabric mod designed for manual and semi-automated ore vein surveying and 3D trajectory analysis in Minecraft. It is specifically designed and tailored for the custom linear ore vein generation mechanics found on **CivNodes** (`play.civnodes.net`) and similar Civ-genre servers. 

When a player discovers exposed diamond ores in tunnels or caves, they can log coordinates manually or use the built-in 50m Auto-Scanner. The mod then computes the 3D best-fit centerline trajectory (via 3D Principal Component Analysis), linear vein density (`ores/m`), statistical cutoff boundaries, and optimal tunnel cross-section (`±R` scatter) to follow the vein with maximum efficiency.

---

## 1. Designed for CivNodes Ore Generation

Unlike standard vanilla Minecraft ore blobs, CivNodes utilizes directional, continuous subterranean ore veins that follow distinct geological trajectories through deepslate, dynamically spawning diamond ores upon adjacent block excavation.

**Vein Surveyor** is built specifically around this system:
- **Trend Line Extraction:** Extracts the 3D strike vector $\vec{v}$ of continuous CivNodes veins from as few as 2 sampled ore blocks.
- **Retroactive 50m Auto-Scanner:** Instantly sweeps up to 50 meters of old or previously excavated mine shafts, importing all revealed diamond ore blocks into the active vein session in milliseconds.
- **Multi-Vein Surveying:** Supports independent, concurrent vein sessions (`Vein 1`, `Vein 2`, `Vein 3`), allowing players to track intersecting or neighboring veins simultaneously without losing previous data.
- **Vein Density & Spacing:** Computes linear yield ($\text{ores}/\text{meter}$) and average spacing ($\mu_{\text{gap}}$) characteristic of the server's vein algorithms.
- **Over-Tunneling Prevention:** Uses statistical gap analysis to project a **Red Cutoff Marker**, informing the player when a CivNodes vein has reached its natural termination.

---

## 2. Core Features & In-Game Surveying

1. **50m Auto-Detect Mode (`NUMPAD 3`)**:
   - Toggle continuous 50-meter spherical scanning with **`NUMPAD 3`**.
   - Sweeps client chunks every 0.5s for exposed diamond ore blocks and adds them directly to the active vein session.
   - Automatically imports historical ores when walking into an old tunnel, instantly reconstructing the vein's past trajectory.

2. **Manual Block Tagging (`NUMPAD 0`)**:
   - Aim at an exposed ore block in line of sight and tap **`NUMPAD 0`**.
   - Captures the targeted `(X, Y, Z)` coordinate into the active vein session and plays an audio chime.
   - In-world bounding boxes are highlighted around all surveyed points.

3. **Multi-Vein Session Management**:
   - Create independent vein sessions (`Vein 1`, `Vein 2`, `Vein 3`) with **`NUMPAD 4`**.
   - Delete the active vein session with **`NUMPAD 5`**.
   - Cycle the active session with **`NUMPAD 6`** to switch which vein is actively surveyed and rendered.
   - Operations like undo (`NUMPAD 1`) and clear (`NUMPAD 2`) apply strictly to the active session.

4. **3D Centerline Calculation (PCA Regression)**:
   - Once $\ge 2$ ore blocks are tagged in a session, the mod runs 3D Principal Component Analysis (covariance matrix diagonalization) to determine the true orientation vector $\vec{v}$ and centroid $\vec{c}$.
   - Renders a multi-zone color-coded trajectory line showing the vein's core path, high-probability reach, and statistical cutoff boundary.

5. **Linear Vein Density (`ores/m`) & Gap Analysis**:
   - Calculates the 1D span length along the vein axis ($L = t_{\max} - t_{\min}$), linear concentration ($\rho = (N - 1) / L$), and average spacing between ores ($\mu_{\text{gap}} = 1 / \rho$).
   - Computes a statistical **Cutoff Distance** ($3.5 \times \mu_{\text{gap}}$): if no ores appear within this distance, the survey line turns red with a **Red Termination Stop Marker**, preventing unproductive over-tunneling.

6. **Radial Scatter & Containment Envelope (`±R`)**:
   - Computes the perpendicular distance $d_i$ from each surveyed ore block to the best-fit line.
   - Calculates average scatter and maximum scatter radius to recommend the minimum tunnel dimensions (e.g. `Dig 2x2 tunnel` or `Dig 3x3 tunnel`) necessary to capture all off-axis ore branches.
   - Renders wireframe cross-sectional rings along the trajectory.

7. **Compact Glass HUD Overlay (`NUMPAD 7`)**:
   - Displays real-time statistics for the active vein session in the top-left corner:
     - Active session name, Auto-Scan status (`Auto: ON`), and projection reach
     - Vein span length ($m$)
     - Trajectory compass heading (e.g., `ENE (68.2°, Pitch -14.5°)`)
     - Linear density (`ores/m`) & expected gap
     - Predicted cutoff distance
     - Radial scatter radius & suggested excavation profile
     - $R^2$ fit confidence bar
     - Multi-session status bar showing sample counts across all tracked veins (e.g., `Veins: ▶[Vein 1: 5]  [Vein 2: 3]`)

---

## 3. Compliance & Fair Play

- **Exposed / Client-Loaded Blocks Only:** The mod does not inspect unexposed or ungenerated blocks inside solid rock. On dynamic ore generation servers like CivNodes, diamond blocks are only present in client chunks once exposed through player excavation.
- **No Packet Manipulation / Zero Server Traffic:** No packets are modified, intercepted, or sent to the server. All calculations and overlays occur strictly on the client.
- **No Automation of Mining/Movement:** The mod does not automate player actions, mining, or movement; it is an analytical calculator and visual survey instrument.
- **Open Source:** The complete source code is publicly hosted on GitHub / Forgejo for full transparency.

---

## 4. Mathematical Methodology

### A. Centroid
Given $N$ sampled ore positions $\mathbf{p}_i = (x_i, y_i, z_i)$:
$$\mathbf{c} = \frac{1}{N} \sum_{i=1}^N \mathbf{p}_i$$

### B. 3D Covariance Matrix
$$\mathbf{C} = \frac{1}{N} \sum_{i=1}^N (\mathbf{p}_i - \mathbf{c})(\mathbf{p}_i - \mathbf{c})^T$$

### C. Dominant Eigenvector (Vein Direction)
The vein's primary axis $\mathbf{v} = (v_x, v_y, v_z)$ is the dominant eigenvector of $\mathbf{C}$, extracted via power iteration:
$$\mathbf{v}_{k+1} = \frac{\mathbf{C} \mathbf{v}_k}{\|\mathbf{C} \mathbf{v}_k\|}$$

### D. 1D Projections & Linear Density
Each sample $\mathbf{p}_i$ is projected onto the line:
$$t_i = (\mathbf{p}_i - \mathbf{c}) \cdot \mathbf{v}$$
$$\text{Span Length } L = \max(t_i) - \min(t_i)$$
$$\text{Linear Density } \rho = \frac{N - 1}{L} \quad (\text{ores / meter})$$

### E. Radial Scatter & Bounding Radius
The perpendicular offset $d_i$ of each point from the vein centerline:
$$d_i = \|(\mathbf{p}_i - \mathbf{c}) - t_i \mathbf{v}\|$$
$$R_{\text{max}} = \max(d_i), \quad R_{\text{avg}} = \frac{1}{N} \sum_{i=1}^N d_i$$

---

## 5. Default Keybindings (Numpad Layout)

| Key | Action | Description |
|---|---|---|
| **`NUMPAD 0`** | Survey Ore Block | Tags the targeted ore block into the active vein session. |
| **`NUMPAD 1`** | Undo Point | Removes the most recently added point from the active session. |
| **`NUMPAD 2`** | Clear Active Vein | Clears all points in the active session and resets its calculation. |
| **`NUMPAD 3`** | Toggle Auto-Detect | Toggles continuous 50m auto-scanning for exposed diamond ores. |
| **`NUMPAD 4`** | New Vein Session | Creates a new auto-numbered vein session (`Vein 2`, `Vein 3`, etc.) and activates it. |
| **`NUMPAD 5`** | Delete Vein Session | Deletes the active vein session and switches to the adjacent session. |
| **`NUMPAD 6`** | Cycle Vein Session | Cycles the active session among all tracked veins. |
| **`NUMPAD 7`** | Toggle HUD | Shows or hides the top-left stats panel. |
| **`NUMPAD 8`** | Cycle Reach | Cycles projection reach (25m $\rightarrow$ 50m $\rightarrow$ 100m $\rightarrow$ 200m). |

*(All keys can also be rebound in Minecraft's **Options $\rightarrow$ Controls $\rightarrow$ Key Binds $\rightarrow$ Vein Surveyor** menu).*

---

## 6. Technical Details & Build Instructions

- **Engine:** Fabric Loader 0.16.x+ / 0.19.x+
- **Minecraft Version:** 1.21.11
- **Java Version:** 21 (OpenJDK)
- **Dependencies:** Fabric API (`fabric-api`)
- **Build Command:**
  ```bash
  ./gradlew build
  ```
  The compiled `.jar` file will be generated in `build/libs/veinsurveyor-1.2.0.jar`.
