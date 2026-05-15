import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-learn-more',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './learn-more.component.html'
})
export class LearnMoreComponent {
  year = new Date().getFullYear();
}
