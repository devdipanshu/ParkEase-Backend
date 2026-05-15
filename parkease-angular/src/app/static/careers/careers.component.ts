import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-careers',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './careers.component.html'
})
export class CareersComponent {
  year = new Date().getFullYear();
}
