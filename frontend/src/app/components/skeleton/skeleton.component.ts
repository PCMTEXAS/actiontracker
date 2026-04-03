import { Component, input } from '@angular/core';

/**
 * Reusable skeleton placeholder that renders Bootstrap placeholder-glow rows.
 *
 * Usage:
 *   <app-skeleton variant="table" [rows]="5" />
 *   <app-skeleton variant="cards" [count]="4" />
 *   <app-skeleton variant="detail" />
 */
@Component({
  selector: 'app-skeleton',
  standalone: true,
  template: `
    @switch (variant()) {

      <!-- Table skeleton: N shimmer rows -->
      @case ('table') {
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th style="width:36px;"><span class="placeholder col-12 placeholder-sm"></span></th>
                <th style="width:35%;"><span class="placeholder col-8 placeholder-sm"></span></th>
                <th><span class="placeholder col-6 placeholder-sm"></span></th>
                <th><span class="placeholder col-5 placeholder-sm"></span></th>
                <th><span class="placeholder col-4 placeholder-sm"></span></th>
                <th><span class="placeholder col-6 placeholder-sm"></span></th>
                <th><span class="placeholder col-4 placeholder-sm"></span></th>
                <th style="width:90px;"></th>
              </tr>
            </thead>
            <tbody class="placeholder-glow">
              @for (_ of rowArray(); track $index) {
                <tr>
                  <td><span class="placeholder col-12"></span></td>
                  <td>
                    <span class="placeholder col-{{ 6 + ($index % 4) }}"></span>
                  </td>
                  <td><span class="placeholder col-8"></span></td>
                  <td><span class="placeholder col-6"></span></td>
                  <td><span class="placeholder col-5 rounded-pill"></span></td>
                  <td><span class="placeholder col-7 rounded-pill"></span></td>
                  <td><span class="placeholder col-5"></span></td>
                  <td></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Dashboard stat cards skeleton -->
      @case ('cards') {
        <div class="row g-3 mb-4 placeholder-glow">
          @for (_ of countArray(); track $index) {
            <div class="col-6 col-md-3">
              <div class="card h-100">
                <div class="card-body text-center py-3">
                  <div class="display-6 fw-bold mb-1">
                    <span class="placeholder col-4"></span>
                  </div>
                  <div class="small">
                    <span class="placeholder col-8"></span>
                  </div>
                </div>
              </div>
            </div>
          }
        </div>
      }

      <!-- Task detail skeleton -->
      @case ('detail') {
        <div class="placeholder-glow">
          <!-- Breadcrumb -->
          <div class="mb-3">
            <span class="placeholder col-3"></span>
          </div>
          <!-- Title area -->
          <div class="mb-4">
            <span class="placeholder col-2 me-2 rounded-pill"></span>
            <span class="placeholder col-2 rounded-pill"></span>
            <div class="mt-2">
              <span class="placeholder col-7 fs-4"></span>
            </div>
          </div>
          <!-- Two-column -->
          <div class="row g-4">
            <div class="col-12 col-lg-7">
              <div class="card">
                <div class="card-header"><span class="placeholder col-3"></span></div>
                <div class="card-body">
                  <span class="placeholder col-12 mb-2 d-block"></span>
                  <span class="placeholder col-10 mb-2 d-block"></span>
                  <span class="placeholder col-8 mb-4 d-block"></span>
                  <div class="row g-3">
                    <div class="col-sm-6">
                      <span class="placeholder col-5 mb-1 d-block"></span>
                      <span class="placeholder col-8"></span>
                    </div>
                    <div class="col-sm-6">
                      <span class="placeholder col-5 mb-1 d-block"></span>
                      <span class="placeholder col-7"></span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="col-12 col-lg-5">
              <div class="card" style="min-height: 300px;">
                <div class="card-header"><span class="placeholder col-4"></span></div>
                <div class="card-body">
                  @for (_ of [1,2,3]; track $index) {
                    <div class="d-flex gap-2 mb-3">
                      <span class="placeholder rounded-circle flex-shrink-0" style="width:30px;height:30px;"></span>
                      <div class="flex-grow-1">
                        <span class="placeholder col-4 mb-1 d-block"></span>
                        <span class="placeholder col-9 d-block"></span>
                      </div>
                    </div>
                  }
                </div>
              </div>
            </div>
          </div>
        </div>
      }

      <!-- Dashboard body skeleton (table + activity) -->
      @case ('dashboard') {
        <div class="placeholder-glow">
          <div class="row g-4">
            <div class="col-12 col-xl-8">
              <div class="card">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                  <span class="placeholder col-3"></span>
                  <span class="placeholder col-2 rounded"></span>
                </div>
                <div class="card-body p-0">
                  <table class="table align-middle mb-0">
                    <tbody>
                      @for (_ of [1,2,3,4,5]; track $index) {
                        <tr>
                          <td style="width:40%;"><span class="placeholder col-{{ 7 + ($index % 3) }}"></span></td>
                          <td><span class="placeholder col-6"></span></td>
                          <td><span class="placeholder col-5 rounded-pill"></span></td>
                          <td><span class="placeholder col-7 rounded-pill"></span></td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            <div class="col-12 col-xl-4">
              <div class="card">
                <div class="card-header bg-white"><span class="placeholder col-4"></span></div>
                <ul class="list-group list-group-flush">
                  @for (_ of [1,2,3,4,5]; track $index) {
                    <li class="list-group-item py-2 px-3">
                      <div class="d-flex gap-2">
                        <span class="placeholder rounded-circle flex-shrink-0" style="width:28px;height:28px;"></span>
                        <div class="flex-grow-1">
                          <span class="placeholder col-8 d-block mb-1"></span>
                          <span class="placeholder col-5 d-block"></span>
                        </div>
                      </div>
                    </li>
                  }
                </ul>
              </div>
            </div>
          </div>
        </div>
      }
    }
  `,
})
export class SkeletonComponent {
  readonly variant = input<'table' | 'cards' | 'detail' | 'dashboard'>('table');
  readonly rows = input<number>(5);
  readonly count = input<number>(4);

  protected rowArray(): number[] {
    return Array.from({ length: this.rows() });
  }

  protected countArray(): number[] {
    return Array.from({ length: this.count() });
  }
}
