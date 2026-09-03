# Vein Surveyor

**Vein Surveyor** is a client-side Fabric mod designed for manual ore vein surveying and 3D trajectory analysis in Minecraft. When a player discovers exposed diamond ores in tunnels or caves, they can manually log the coordinates of visible blocks. The mod then computes the 3D best-fit centerline trajectory (via 3D Principal Component Analysis), linear vein density (`ores/m`), statistical cutoff boundaries, and optimal tunnel cross-section (`±R` scatter).

---

## 1. Overview & Core Features

Vein Surveyor replaces manual pen-and-paper 3D line-fitting with real-time in-game statistical analysis:

1. **Manual Block Tagging (`V`)**:
   - Aim at an exposed ore block in line of sight and tap `V`.
   - The mod captures the targeted `(X, Y, Z)` coordinate and plays an audio chime.
   - Bounding boxes are highlighted around all surveyed points.

2. **3D Centerline Calculation (PCA Regression)**:
   - Once $\ge 2$ ore blocks are tagged, the mod runs 3D Principal Component Analysis (covariance matrix diagonalization) to determine the true orientation vector $\vec{v}$ and centroid $\vec{c}$.
   - Renders a multi-zone color-coded trajectory line showing the vein's core path, high-probability reach, and statistical cutoff boundary.

3. **Linear Vein Density (`ores/m`) & Gap Analysis**:
   - Calculates the 1D span length along the vein axis ($L = t_{\max} - t_{\min}$), linear concentration ($\rho = N / L$), and average spacing between ores ($\mu_{\text{gap}} = 1 / \rho$).
   - Computes a statistical **Cutoff Distance** ($3.5 \times \mu_{\text{gap}}$): if no ores appear within this distance, the survey line turns red with a **Red Termination Stop Marker**, preventing unproductive over-tunneling.

4. **Radial Scatter & Containment Envelope (`±R`)**:
   - Computes the perpendicular distance $d_i$ from each surveyed ore block to the best-fit line.
   - Calculates average scatter and maximum scatter radius to recommend the minimum tunnel dimensions (e.g. `Dig 2x2 tunnel` or `Dig 3x3 tunnel`) necessary to capture all off-axis ore branches.
   - Renders wireframe cross-sectional rings along the trajectory.

5. **Compact Glass HUD Overlay (`H`)**:
   - Displays real-time statistics in the top-left corner:
     - Sample count & vein span ($m$)
     - Trajectory compass heading (e.g., `ENE (68.2°, Pitch -14.5°)`)
     - Linear density (`ores/m`) & expected gap
     - Predicted cutoff distance
     - Radial scatter radius & suggested excavation profile
     - $R^2$ fit confidence bar

6. **Full Session Control**:
   - `Z`: Undo the last recorded survey point.
   - `C`: Clear all surveyed points and reset the workspace.
   - `J`: Cycle forward/backward projection reach (25m, 50m, 100m, 200m).
   - `H`: Toggle HUD overlay visibility.

---

## 2. Compliance & Fair Play

- **100% Manual Input:** The mod does **not** search chunks, peek through solid blocks, or inspect block data in unexposed areas. Coordinates are only recorded when the player physically aims their crosshair directly at a visible block and presses the survey key (`V`).
- **No Packet Manipulation / Zero Server Traffic:** No packets are modified, intercepted, or sent to the server. All calculations and overlays occur strictly on the client.
- **No Automation / No Botting:** The mod does not automate mining, movement, or player actions; it is purely an analytical calculator and visual ruler.
- **Open Source:** The complete source code is publicly hosted on GitHub / Forgejo for full transparency.

---

## 3. Mathematical Methodology

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
$$\text{Linear Density } \rho = \frac{N}{L} \quad (\text{ores / meter})$$

### E. Radial Scatter & Bounding Radius
The perpendicular offset $d_i$ of each point from the vein centerline:
$$d_i = \|(\mathbf{p}_i - \mathbf{c}) - t_i \mathbf{v}\|$$
$$R_{\text{max}} = \max(d_i), \quad R_{\text{avg}} = \frac{1}{N} \sum_{i=1}^N d_i$$

---

## 4. Default Keybindings

| Key | Action | Description |
|---|---|---|
| **`V`** | Survey Ore Block | Tags the block currently targeted by the crosshair. |
| **`Z`** | Undo Point | Removes the most recently added survey point. |
| **`C`** | Clear Survey | Clears all survey points and resets the calculation. |
| **`H`** | Toggle HUD | Shows or hides the top-left stats panel. |
| **`J`** | Cycle Reach | Cycles projection reach (25m $\rightarrow$ 50m $\rightarrow$ 100m $\rightarrow$ 200m). |

---

## 5. Technical Details & Build Instructions

- **Engine:** Fabric Loader 0.16.x+ / 0.19.x+
- **Minecraft Version:** 1.21.11
- **Java Version:** 21 (OpenJDK)
- **Dependencies:** Fabric API (`fabric-api`)
- **Build Command:**
  ```bash
  ./gradlew build
  ```
  The compiled `.jar` file will be generated in `build/libs/veinsurveyor-1.0.0.jar`.
