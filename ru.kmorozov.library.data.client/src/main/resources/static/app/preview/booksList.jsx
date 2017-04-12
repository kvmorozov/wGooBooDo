import React from "react";
import {Item} from "semantic-ui-react";

class BooksList extends React.Component {

    constructor(props) {
        super(props);
    }

    update = (books) => {
        this.setState({books: books});
    }

    getDisplayItem(book) {
        let imgPath;
        if (book.title.endsWith('pdf'))
            imgPath = '/icons/pdf.png';
        else if (book.title.endsWith('djvu'))
            imgPath = '/icons/djvu.png';

        return (
            <Item key={book.id}>
                <Item.Image size='tiny' src={imgPath} />

                <Item.Content>
                    <Item.Header as='a'>Header</Item.Header>
                    <Item.Meta>Description</Item.Meta>
                    <Item.Description>
                        {book.title}
                    </Item.Description>
                    <Item.Extra>Additional Details</Item.Extra>
                </Item.Content>
            </Item>
        )
    }

    render() {
        if (this.state == null || this.state.books == null)
            return <Item.Group></Item.Group>
        else {
            let books = this.state.books;
            let cells = books.map(book => this.getDisplayItem(book));

            return (
                <Item.Group>
                    {cells}
                </Item.Group>
            )
        }
    }
}

export default BooksList;