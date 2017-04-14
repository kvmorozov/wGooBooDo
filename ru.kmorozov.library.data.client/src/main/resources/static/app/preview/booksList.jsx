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
        switch (book.format) {
            case 'PDF':
                imgPath = '/icons/pdf.png';
                break;
            case 'DJVU':
                imgPath = '/icons/djvu.png';
                break;
        }

        return (
            <Item key={book.id}>
                <Item.Image size='mini' src={imgPath}/>

                <Item.Content>
                    <Item.Header as='a' onclick={}>{book.title}</Item.Header>
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