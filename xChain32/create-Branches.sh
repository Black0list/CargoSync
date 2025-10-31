#!/bin/bash

# === CLIENT EPIC ===
git branch feature/LOG-18-track-shipment

# === WAREHOUSE_MANAGER EPIC ===
git branch feature/LOG-23-inbound-reception
git branch feature/LOG-26-outbound-shipment
git branch feature/LOG-29-adjust-stock
git branch feature/LOG-32-reserve-salesorder
git branch feature/LOG-35-create-shipment
git branch feature/LOG-38-update-shipment-status

# === ADMIN EPIC ===
git branch feature/LOG-43-manage-products
git branch feature/LOG-46-manage-warehouses
git branch feature/LOG-49-create-purchase-order
git branch feature/LOG-52-cancel-order

# === Push branches to remote (GitHub) ===
for branch in $(git branch | grep feature/); do
  git push origin ${branch#* }
done
