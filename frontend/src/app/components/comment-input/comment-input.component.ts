import { Component, OnInit, inject, input, output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommentService } from '../../services/comment.service';
import { UserService } from '../../services/user.service';
import { Comment } from '../../models/comment.model';
import { AppUser } from '../../models/user.model';

@Component({
  selector: 'app-comment-input',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comment-input.component.html',
})
export class CommentInputComponent implements OnInit {
  private readonly commentService = inject(CommentService);
  private readonly userService = inject(UserService);

  readonly taskId = input.required<string>();
  readonly commentAdded = output<Comment>();

  protected readonly body = signal<string>('');
  protected readonly mentionSearch = signal<string | null>(null);
  protected readonly cursorPosition = signal<number>(0);
  protected readonly submitting = signal<boolean>(false);
  protected readonly allMembers = signal<AppUser[]>([]);

  protected readonly filteredMembers = computed<AppUser[]>(() => {
    const search = this.mentionSearch();
    if (search === null) return [];
    const lower = search.toLowerCase();
    return this.allMembers().filter((m) =>
      m.name.toLowerCase().includes(lower) || m.email.toLowerCase().includes(lower)
    );
  });

  ngOnInit(): void {
    this.userService.getTeamMembers().subscribe({
      next: (members) => this.allMembers.set(members),
      error: () => {},
    });
  }

  protected onBodyInput(event: Event): void {
    const el = event.target as HTMLTextAreaElement;
    const text = el.value;
    const pos = el.selectionStart ?? 0;
    this.body.set(text);
    this.cursorPosition.set(pos);

    // Detect @ mention trigger
    const before = text.slice(0, pos);
    const atIndex = before.lastIndexOf('@');
    if (atIndex !== -1) {
      const after = before.slice(atIndex + 1);
      // Only trigger if no space after @
      if (!after.includes(' ')) {
        this.mentionSearch.set(after);
        return;
      }
    }
    this.mentionSearch.set(null);
  }

  protected selectMention(user: AppUser): void {
    const text = this.body();
    const pos = this.cursorPosition();
    const before = text.slice(0, pos);
    const atIndex = before.lastIndexOf('@');
    const after = text.slice(pos);
    const newText = text.slice(0, atIndex) + '@' + user.name + ' ' + after;
    this.body.set(newText);
    this.mentionSearch.set(null);
  }

  protected submit(): void {
    const text = this.body().trim();
    if (!text) return;
    this.submitting.set(true);
    this.commentService.addComment(this.taskId(), text).subscribe({
      next: (comment) => {
        this.body.set('');
        this.submitting.set(false);
        this.commentAdded.emit(comment);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  protected onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      this.submit();
    }
    if (event.key === 'Escape') {
      this.mentionSearch.set(null);
    }
  }
}
